package com.scau.village.module.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.common.result.Result;
import com.scau.village.common.utils.JwtUtils;
import com.scau.village.common.utils.WechatUtil;
import com.scau.village.module.auth.dto.LoginDto;
import com.scau.village.module.auth.dto.WechatLoginDto;
import com.scau.village.module.auth.dto.WechatPhoneLoginDto;
import com.scau.village.module.auth.dto.WechatRegisterDto;
import com.scau.village.module.auth.vo.WechatLoginVO;
import com.scau.village.module.resident.entity.ResidentProfile;
import com.scau.village.module.resident.service.ResidentProfileService;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final WechatUtil wechatUtil;
    private final ResidentProfileService residentProfileService;

    static {
        // 添加 BouncyCastle 安全提供者（用于 AES 解密）
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 管理员/村民手机号密码登录（原有）
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto dto) {
        User user = userService.findByPhone(dto.getPhone());
        if (user == null) {
            throw new BusinessException("手机号未注册");
        }

        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        log.info("登录尝试，手机号: {}, 匹配结果: {}", dto.getPhone(), matches);

        if (!matches) {
            throw new BusinessException("手机号或密码错误");
        }

        String token = jwtUtils.generateToken(Long.valueOf(user.getId()), user.getRole(), user.getTenantId());
        return Result.success(token);
    }

    /**
     * 微信登录（首次授权，检查是否已注册）
     */
    @PostMapping("/wechat-login")
    public Result<WechatLoginVO> wechatLogin(@Valid @RequestBody WechatLoginDto dto) {
        JSONObject result = wechatUtil.getOpenidAndSessionKey(dto.getCode());
        String openid = result.getString("openid");

        User user = userService.getByOpenid(openid);
        if (user != null) {
            // 已注册，生成 token
            String token = jwtUtils.generateToken(user.getId().longValue(), user.getRole(), user.getTenantId());
            return Result.success(new WechatLoginVO(true, token, openid));
        } else {
            // 未注册，返回 openid 供后续注册
            return Result.success(new WechatLoginVO(false, null, openid));
        }
    }

    /**
     * 微信注册（完善信息并绑定档案）
     */
    @PostMapping("/wechat-register")
    public Result<String> wechatRegister(@Valid @RequestBody WechatRegisterDto dto) {
        // 1. 检查 openid 是否已被注册（防止并发）
        User existing = userService.getByOpenid(dto.getOpenid());
        if (existing != null) {
            return Result.error(400, "该微信已注册，请直接登录");
        }

        // 2. 匹配居民档案（根据手机号和户主姓名）
        Integer tenantId = 1; // 默认龙胜村，可考虑从配置或请求头获取
        ResidentProfile profile = residentProfileService.matchByPhoneAndOwner(tenantId, dto.getPhone(), dto.getOwnerName());
        if (profile == null) {
            return Result.error(400, "未找到匹配的档案信息，请确认手机号和户主姓名一致，或联系村委");
        }

        // 3. 检查手机号是否已被其他用户占用（但档案绑定的手机号可能已存在注册，但允许重新绑定？此处视为同一人）
        // 如果已存在相同手机号的用户，可考虑合并，但简单起见，先检查是否被占用
        User phoneUser = userService.findByPhone(dto.getPhone());
        if (phoneUser != null) {
            // 如果该手机号已有用户，但该用户未绑定 openid，可以更新其 openid；否则报错
            if (StringUtils.isBlank(phoneUser.getOpenid())) {
                // 更新该用户的 openid 和档案关联
                phoneUser.setOpenid(dto.getOpenid());
                phoneUser.setResidentProfileId(profile.getId());
                userService.updateById(phoneUser);
                String token = jwtUtils.generateToken(phoneUser.getId().longValue(), phoneUser.getRole(), phoneUser.getTenantId());
                return Result.success(token);
            } else {
                return Result.error(400, "该手机号已被其他微信绑定，请联系村委");
            }
        }

        // 4. 创建新用户
        User user = new User();
        user.setOpenid(dto.getOpenid());
        user.setPhone(dto.getPhone());
        user.setRealName(dto.getRealName());
        user.setTenantId(tenantId);
        user.setRole("VILLAGER");
        user.setResidentProfileId(profile.getId());
        user.setPoints(0);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);

        // 5. 生成 token
        String token = jwtUtils.generateToken(user.getId().longValue(), user.getRole(), user.getTenantId());
        log.info("微信注册成功，userId={}, openid={}, phone={}", user.getId(), dto.getOpenid(), dto.getPhone());
        return Result.success(token);
    }

    // =========================================================
    // 微信手机号一键登录（支持自动注册）
    // =========================================================

    /**
     * 微信手机号授权登录（一键登录）
     * 前端调用 wx.getPhoneNumber 获取 encryptedData 和 iv，传给后端解密获取手机号
     * 若手机号未注册，自动创建用户（无需额外注册流程）
     */
    @PostMapping("/wechat-phone-login")
    public Result<?> wechatPhoneLogin(@Valid @RequestBody WechatPhoneLoginDto dto) {
        log.info("微信手机号授权登录请求: code={}", dto.getCode());

        // 1. 参数校验
        if (StringUtils.isBlank(dto.getCode())) {
            return Result.error(400, "code 不能为空");
        }
        if (StringUtils.isBlank(dto.getEncryptedData())) {
            return Result.error(400, "encryptedData 不能为空");
        }
        if (StringUtils.isBlank(dto.getIv())) {
            return Result.error(400, "iv 不能为空");
        }

        // 2. 用 code 换取 openid 和 session_key
        JSONObject wxResult;
        try {
            wxResult = wechatUtil.getOpenidAndSessionKey(dto.getCode());
        } catch (Exception e) {
            log.error("获取 session_key 失败", e);
            return Result.error(500, "微信登录失败: " + e.getMessage());
        }

        String openid = wxResult.getString("openid");
        String sessionKey = wxResult.getString("session_key");

        if (StringUtils.isBlank(openid) || StringUtils.isBlank(sessionKey)) {
            log.error("微信返回数据异常: openid={}, sessionKey={}", openid, sessionKey);
            return Result.error(500, "微信登录失败: 获取 openid 或 session_key 失败");
        }

        log.info("微信获取 openid: {}", openid);

        // 3. 解密手机号
        String phone;
        try {
            phone = decryptPhoneNumber(dto.getEncryptedData(), dto.getIv(), sessionKey);
            log.info("解密获取手机号: {}", phone);
        } catch (Exception e) {
            log.error("解密手机号失败", e);
            return Result.error(500, "手机号解密失败: " + e.getMessage());
        }

        if (StringUtils.isBlank(phone)) {
            return Result.error(500, "解密手机号失败: 手机号为空");
        }

        // 4. 查询用户
        // 4.1 先用手机号查询
        User userByPhone = userService.findByPhone(phone);

        if (userByPhone != null) {
            // 场景A：手机号在库中 → 更新 openid，登录成功
            if (StringUtils.isBlank(userByPhone.getOpenid())) {
                userByPhone.setOpenid(openid);
                userService.updateById(userByPhone);
                log.info("老用户首次微信登录，绑定 openid: userId={}, phone={}", userByPhone.getId(), phone);
            } else if (!openid.equals(userByPhone.getOpenid())) {
                // 场景D：手机号在库中，但 openid 不一致 → 以 openid 为准，更新 openid
                log.warn("手机号 {} 被新的 openid 绑定，即将更新 openid", phone);
                userByPhone.setOpenid(openid);
                userService.updateById(userByPhone);
            }
            // 生成 Token
            String token = jwtUtils.generateToken(
                userByPhone.getId().longValue(),
                userByPhone.getRole(),
                userByPhone.getTenantId()
            );
            return Result.success(token);
        }

        // 4.2 手机号查不到，用 openid 查
        User userByOpenid = userService.getByOpenid(openid);
        if (userByOpenid != null) {
            // 场景D：openid 在库中，但手机号变了 → 更新手机号
            if (!phone.equals(userByOpenid.getPhone())) {
                userByOpenid.setPhone(phone);
                userService.updateById(userByOpenid);
                log.info("用户换手机号: userId={}, 新号={}", userByOpenid.getId(), phone);
            }
            String token = jwtUtils.generateToken(
                userByOpenid.getId().longValue(),
                userByOpenid.getRole(),
                userByOpenid.getTenantId()
            );
            return Result.success(token);
        }

        // 5. 手机号查不到，openid 也查不到 → 自动创建用户并登录（无需额外注册）
        log.info("新用户微信登录，自动注册: phone={}, openid={}", phone, openid);

        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setPhone(phone);
        newUser.setRealName("");  // 默认空，可让用户后续通过个人资料完善
        newUser.setTenantId(1);   // 默认龙胜村
        newUser.setRole("VILLAGER");
        newUser.setPoints(0);
        newUser.setDeleted(0);
        newUser.setCreateTime(LocalDateTime.now());
        // 不绑定居民档案，暂不设置 residentProfileId
        userService.save(newUser);

        String token = jwtUtils.generateToken(
            newUser.getId().longValue(),
            newUser.getRole(),
            newUser.getTenantId()
        );
        log.info("微信自动注册成功，userId={}, openid={}, phone={}", newUser.getId(), openid, phone);
        return Result.success(token);
    }

    // =========================================================
    // 私有方法：解密手机号
    // =========================================================

    /**
     * 解密微信手机号
     */
    private String decryptPhoneNumber(String encryptedData, String iv, String sessionKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
        byte[] ivBytes = Base64.getDecoder().decode(iv);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decryptedJson = new String(decryptedBytes, "UTF-8");

        JSONObject json = JSONObject.parseObject(decryptedJson);
        JSONObject phoneInfo = json.getJSONObject("phoneInfo");

        if (phoneInfo == null) {
            // 兼容老版本结构
            return json.getString("phoneNumber");
        }

        return phoneInfo.getString("phoneNumber");
    }
}