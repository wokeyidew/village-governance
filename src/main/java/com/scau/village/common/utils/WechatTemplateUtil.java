package com.scau.village.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信订阅消息工具类
 * 用于发送微信小程序订阅消息
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Component
public class WechatTemplateUtil {

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 缓存 access_token，key 为 appid，value 为 token 及过期时间
    private static final Map<String, TokenCache> TOKEN_CACHE = new ConcurrentHashMap<>();

    private static final long EXPIRE_SAFE_MARGIN = 10 * 60 * 1000; // 提前10分钟刷新

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    private static final String SEND_SUBSCRIBE_MSG_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token={access_token}";

    /**
     * 获取微信 access_token（带缓存）
     *
     * @return access_token
     */
    public String getAccessToken() {
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            throw new RuntimeException("微信配置缺失，请检查 wechat.appid 和 wechat.secret");
        }

        TokenCache cache = TOKEN_CACHE.get(appid);
        if (cache != null && System.currentTimeMillis() < cache.getExpireTime()) {
            return cache.getToken();
        }

        // 重新获取
        synchronized (this) {
            // 双重检查
            cache = TOKEN_CACHE.get(appid);
            if (cache != null && System.currentTimeMillis() < cache.getExpireTime()) {
                return cache.getToken();
            }

            String url = TOKEN_URL.replace("{appid}", appid).replace("{secret}", secret);
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    JSONObject json = JSON.parseObject(response.getBody());
                    if (json.containsKey("access_token")) {
                        String token = json.getString("access_token");
                        int expiresIn = json.getIntValue("expires_in");
                        long expireTime = System.currentTimeMillis() + (expiresIn * 1000L) - EXPIRE_SAFE_MARGIN;
                        TOKEN_CACHE.put(appid, new TokenCache(token, expireTime));
                        log.info("获取微信 access_token 成功，有效期 {} 秒", expiresIn);
                        return token;
                    } else {
                        String errMsg = json.getString("errmsg");
                        throw new RuntimeException("获取 access_token 失败：" + errMsg);
                    }
                } else {
                    throw new RuntimeException("获取 access_token HTTP 错误：" + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("获取 access_token 异常", e);
                throw new RuntimeException("获取 access_token 异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 发送订阅消息
     *
     * @param openid     接收人 openid
     * @param templateId 模板ID
     * @param pagePath   跳转页面路径（如：pages/index/index）
     * @param data       模板数据，格式为 Map<String, Map<String, String>>，每个值包含 value 和 color
     *                   例如：{"thing1": {"value": "兑换商品"}, "time2": {"value": "2026-07-18 10:00"}}
     */
    public void sendSubscribeMessage(String openid, String templateId, String pagePath, Object data) {
        if (openid == null || openid.isEmpty()) {
            throw new IllegalArgumentException("openid 不能为空");
        }
        if (templateId == null || templateId.isEmpty()) {
            throw new IllegalArgumentException("templateId 不能为空");
        }

        String accessToken = getAccessToken();

        JSONObject requestBody = new JSONObject();
        requestBody.put("touser", openid);
        requestBody.put("template_id", templateId);
        if (pagePath != null && !pagePath.isEmpty()) {
            requestBody.put("page", pagePath);
        }
        // data 字段需要是对象，直接放入
        requestBody.put("data", data);
        // 可选：miniprogram_state 和 lang
        requestBody.put("miniprogram_state", "developer"); // 或 "formal"

        String url = SEND_SUBSCRIBE_MSG_URL.replace("{access_token}", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = JSON.parseObject(response.getBody());
                if (json.containsKey("errcode")) {
                    int errCode = json.getIntValue("errcode");
                    if (errCode == 0) {
                        log.info("订阅消息发送成功，openid={}, templateId={}", openid, templateId);
                    } else {
                        String errMsg = json.getString("errmsg");
                        log.error("订阅消息发送失败，errcode={}, errmsg={}, openid={}, templateId={}",
                                errCode, errMsg, openid, templateId);
                        throw new RuntimeException("订阅消息发送失败：" + errMsg + " (errcode=" + errCode + ")");
                    }
                } else {
                    throw new RuntimeException("微信返回异常：" + response.getBody());
                }
            } else {
                throw new RuntimeException("HTTP 请求失败，状态码：" + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("发送订阅消息异常", e);
            throw new RuntimeException("发送订阅消息异常：" + e.getMessage(), e);
        }
    }

    /**
     * 内部缓存类
     */
    private static class TokenCache {
        private final String token;
        private final long expireTime; // 毫秒时间戳

        public TokenCache(String token, long expireTime) {
            this.token = token;
            this.expireTime = expireTime;
        }

        public String getToken() {
            return token;
        }

        public long getExpireTime() {
            return expireTime;
        }
    }
}