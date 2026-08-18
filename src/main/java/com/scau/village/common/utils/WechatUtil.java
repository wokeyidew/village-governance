package com.scau.village.common.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

/**
 * 微信小程序接口调用工具类
 * 提供获取 openid/session_key、获取手机号、解密手机号的能力
 *
 * @author system
 * @since 2026-07-31
 */
@Slf4j
@Component
public class WechatUtil {

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 注册 BouncyCastle 安全提供者（用于 AES 解密）
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 通过 code 获取微信 openid 和 session_key
     *
     * @param code wx.login 获取的临时凭证
     * @return JSONObject 包含 openid, session_key 等
     * @throws RuntimeException 当接口调用失败时抛出
     */
    public JSONObject getOpenidAndSessionKey(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = JSONObject.parseObject(response.getBody());
                if (json.containsKey("errcode") && json.getInteger("errcode") != 0) {
                    log.error("微信登录失败，errcode={}, errmsg={}", json.getInteger("errcode"), json.getString("errmsg"));
                    throw new RuntimeException("微信登录失败：" + json.getString("errmsg"));
                }
                return json;
            } else {
                log.error("微信接口响应异常，状态码：{}", response.getStatusCode());
                throw new RuntimeException("微信接口调用失败，HTTP状态码：" + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("获取 openid 异常", e);
            throw new RuntimeException("获取 openid 失败：" + e.getMessage(), e);
        }
    }

    /**
     * 通过 getPhoneNumber 返回的 code 获取用户手机号（使用 access_token 方式）
     * 此方式适用于需要单独获取手机号的场景（不依赖解密 encryptedData）
     *
     * @param code 前端通过 getPhoneNumber 组件获取的 code
     * @return 手机号（纯数字字符串）
     * @throws RuntimeException 当接口调用失败或手机号获取失败时抛出
     */
    public String getPhoneNumber(String code) {
        try {
            // 1. 获取 access_token
            String tokenUrl = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appid, secret);
            ResponseEntity<String> tokenResp = restTemplate.getForEntity(tokenUrl, String.class);
            if (tokenResp.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("获取 access_token 失败，HTTP状态码：" + tokenResp.getStatusCode());
            }
            JSONObject tokenJson = JSONObject.parseObject(tokenResp.getBody());
            if (tokenJson.containsKey("errcode")) {
                throw new RuntimeException("获取 access_token 失败：" + tokenJson.getString("errmsg"));
            }
            String accessToken = tokenJson.getString("access_token");

            // 2. 调用获取手机号接口
            String phoneUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject requestBody = new JSONObject();
            requestBody.put("code", code);
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(phoneUrl, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("获取手机号接口响应异常，状态码：" + response.getStatusCode());
            }

            JSONObject result = JSONObject.parseObject(response.getBody());
            Integer errcode = result.getInteger("errcode");
            if (errcode == null || errcode != 0) {
                String errmsg = result.getString("errmsg");
                log.error("获取手机号失败，errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("获取手机号失败：" + (errmsg != null ? errmsg : "未知错误"));
            }

            // 提取手机号（purePhoneNumber 为纯数字，不含国家代码）
            JSONObject phoneInfo = result.getJSONObject("phone_info");
            if (phoneInfo == null) {
                throw new RuntimeException("手机号信息为空");
            }
            String phoneNumber = phoneInfo.getString("purePhoneNumber");
            if (StringUtils.isBlank(phoneNumber)) {
                throw new RuntimeException("手机号为空");
            }
            return phoneNumber;
        } catch (Exception e) {
            log.error("获取微信手机号异常", e);
            throw new RuntimeException("获取微信手机号失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解密微信手机号（通过 encryptedData + iv + sessionKey）
     * 适用于 wx.getPhoneNumber 返回加密数据后，由后端直接解密的场景
     *
     * @param encryptedData wx.getPhoneNumber 返回的 encryptedData
     * @param iv            wx.getPhoneNumber 返回的 iv
     * @param sessionKey    通过 code 换取的 session_key
     * @return 手机号（纯数字字符串）
     * @throws Exception 解密失败时抛出
     */
    public String decryptPhoneNumber(String encryptedData, String iv, String sessionKey) throws Exception {
        if (StringUtils.isAnyBlank(encryptedData, iv, sessionKey)) {
            throw new IllegalArgumentException("encryptedData、iv 和 sessionKey 均不能为空");
        }

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