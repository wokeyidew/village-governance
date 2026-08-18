package com.scau.village.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scau.village.common.exception.BusinessException;
import com.scau.village.module.user.dto.RegisterDto;
import com.scau.village.module.user.dto.UpdateProfileDto;
import com.scau.village.module.user.entity.User;
import com.scau.village.module.user.mapper.UserMapper;
import com.scau.village.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    // 身份证正则（简单校验：18位，最后一位可为X）
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");
    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public User findByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    @Override
    public void saveWithEncryptedPassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
    }

    @Override
    public void updatePassword(Long userId, String rawPassword) {
        User user = getById(userId);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            updateById(user);
        }
    }

    @Override
    @Transactional
    public void register(RegisterDto dto) {
        // 1. 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (count(wrapper) > 0) {
            throw new BusinessException("手机号已注册");
        }

        // 2. 创建新用户
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setTenantId(dto.getTenantId());
        user.setRealName(dto.getRealName());
        user.setAvatar(dto.getAvatar());
        // 默认角色为村民
        user.setRole("VILLAGER");
        user.setPoints(0);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());

        save(user);
    }

    @Override
    @Transactional
    public void updateProfile(Integer userId, UpdateProfileDto dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 更新真实姓名
        if (dto.getRealName() != null && !dto.getRealName().isEmpty()) {
            user.setRealName(dto.getRealName());
        }

        // 2. 更新手机号（需唯一性校验）
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            // 检查手机号是否已被其他用户占用
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, dto.getPhone())
                   .ne(User::getId, userId);
            if (count(wrapper) > 0) {
                throw new BusinessException("手机号已被占用");
            }
            user.setPhone(dto.getPhone());
        }

        // 3. 更新头像
        if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
            user.setAvatar(dto.getAvatar());
        }

        // 4. 修改密码（必须同时提供旧密码和新密码）
        if (dto.getOldPassword() != null && dto.getNewPassword() != null) {
            // 验证旧密码
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new BusinessException("原密码错误");
            }
            // 加密新密码
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        } else if (dto.getOldPassword() != null || dto.getNewPassword() != null) {
            // 只传了一个，提示错误
            throw new BusinessException("修改密码需同时提供旧密码和新密码");
        }

        updateById(user);
    }

    // ==================== 微信登录 & 信息更新（新增） ====================

    @Override
    public User getByOpenid(String openid) {
        if (StringUtils.isBlank(openid)) {
            return null;
        }
        return lambdaQuery().eq(User::getOpenid, openid).one();
    }

    @Override
    @Transactional
    public void updateUserInfo(Integer userId, String phone, String realName, String idCard) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验手机号格式
        if (StringUtils.isNotBlank(phone)) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                throw new BusinessException("手机号格式不正确");
            }
            // 手机号唯一性校验（排除自身）
            long count = lambdaQuery().eq(User::getPhone, phone).ne(User::getId, userId).count();
            if (count > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }

        // 校验姓名长度
        if (StringUtils.isNotBlank(realName)) {
            if (realName.length() > 20) {
                throw new BusinessException("姓名不能超过20个字符");
            }
            user.setRealName(realName);
        }

        // 校验身份证格式（可选）
        if (StringUtils.isNotBlank(idCard)) {
            if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
                throw new BusinessException("身份证格式不正确");
            }
            user.setIdCard(idCard);
        }

        updateById(user);
        log.info("用户信息更新成功，userId={}, phone={}, realName={}", userId, phone, realName);
    }

    @Override
    @Transactional
    public void adminUpdateUserInfo(Integer userId, String phone, String realName, String idCard) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验手机号格式
        if (StringUtils.isNotBlank(phone)) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                throw new BusinessException("手机号格式不正确");
            }
            // 手机号唯一性校验（排除自身）
            long count = lambdaQuery().eq(User::getPhone, phone).ne(User::getId, userId).count();
            if (count > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }

        // 校验姓名长度
        if (StringUtils.isNotBlank(realName)) {
            if (realName.length() > 20) {
                throw new BusinessException("姓名不能超过20个字符");
            }
            user.setRealName(realName);
        }

        // 校验身份证格式（可选）
        if (StringUtils.isNotBlank(idCard)) {
            if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
                throw new BusinessException("身份证格式不正确");
            }
            user.setIdCard(idCard);
        }

        updateById(user);
        log.info("管理员更新用户信息成功，userId={}, phone={}, realName={}", userId, phone, realName);
    }
}