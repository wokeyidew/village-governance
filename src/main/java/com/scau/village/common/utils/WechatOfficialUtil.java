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
 * 公众号模板消息工具类
 * 用于发送微信公众号模板消息（需要绑定公众号）
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Component
public class WechatOfficialUtil {

    @Value("${wechat.official.appid:}")
    private String appid;

    @Value("${wechat.official.secret:}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();

    // 缓存 access_token
    private static final Map<String, TokenCache> TOKEN_CACHE = new ConcurrentHashMap<>();
    private static final long EXPIRE_SAFE_MARGIN = 10 * 60 * 1000; // 提前10分钟刷新

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={appid}&secret={secret}";
    private static final String SEND_TEMPLATE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={access_token}";

    /**
     * 获取 access_token（带缓存）
     */
    public String getAccessToken() {
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            throw new RuntimeException("公众号配置缺失，请检查 wechat.official.appid 和 wechat.official.secret");
        }

        TokenCache cache = TOKEN_CACHE.get(appid);
        if (cache != null && System.currentTimeMillis() < cache.getExpireTime()) {
            return cache.getToken();
        }

        synchronized (this) {
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
                        log.info("获取公众号 access_token 成功，有效期 {} 秒", expiresIn);
                        return token;
                    } else {
                        String errMsg = json.getString("errmsg");
                        throw new RuntimeException("获取公众号 access_token 失败：" + errMsg);
                    }
                } else {
                    throw new RuntimeException("获取公众号 access_token HTTP 错误：" + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("获取公众号 access_token 异常", e);
                throw new RuntimeException("获取公众号 access_token 异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 发送模板消息
     *
     * @param openid     接收用户 openid
     * @param templateId 模板ID
     * @param url        点击模板消息跳转的URL（可选）
     * @param data       模板数据，格式：{"keyword1": {"value": "xxx"}, "keyword2": {"value": "xxx"}}
     * @param miniProgram 小程序跳转配置（可选）：{"appid": "xxx", "pagepath": "xxx"}
     * @return true-发送成功，false-发送失败
     */
    public boolean sendTemplateMessage(String openid, String templateId, String url,
                                       Map<String, Map<String, String>> data,
                                       JSONObject miniProgram) {
        if (openid == null || openid.isEmpty()) {
            log.warn("openid 为空，无法发送模板消息");
            return false;
        }
        if (templateId == null || templateId.isEmpty()) {
            log.warn("templateId 为空，无法发送模板消息");
            return false;
        }

        String accessToken = getAccessToken();

        JSONObject requestBody = new JSONObject();
        requestBody.put("touser", openid);
        requestBody.put("template_id", templateId);
        if (url != null && !url.isEmpty()) {
            requestBody.put("url", url);
        }
        if (miniProgram != null && !miniProgram.isEmpty()) {
            requestBody.put("miniprogram", miniProgram);
        }
        requestBody.put("data", data);

        String sendUrl = SEND_TEMPLATE_URL.replace("{access_token}", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(sendUrl, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject result = JSON.parseObject(response.getBody());
                int errCode = result.getIntValue("errcode");
                if (errCode == 0) {
                    log.info("公众号模板消息发送成功，openid={}, templateId={}", openid, templateId);
                    return true;
                } else {
                    String errMsg = result.getString("errmsg");
                    log.error("公众号模板消息发送失败，errcode={}, errmsg={}, openid={}, templateId={}",
                            errCode, errMsg, openid, templateId);
                    return false;
                }
            } else {
                log.error("公众号模板消息发送 HTTP 请求失败，状态码：{}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("公众号模板消息发送异常", e);
            return false;
        }
    }

    /**
     * 便捷方法：发送兑换通知给管理员
     *
     * @param openid     管理员 openid
     * @param templateId 模板ID（需在公众号后台配置）
     * @param userName   用户姓名
     * @param productName 商品名称
     * @param code       核销码
     */
    public boolean sendExchangeNotification(String openid, String templateId,
                                            String userName, String productName, String code) {
        Map<String, Map<String, String>> data = new java.util.HashMap<>();
        data.put("keyword1", new java.util.HashMap<String, String>() {{
            put("value", userName);
        }});
        data.put("keyword2", new java.util.HashMap<String, String>() {{
            put("value", productName);
        }});
        data.put("keyword3", new java.util.HashMap<String, String>() {{
            put("value", code);
        }});
        data.put("keyword4", new java.util.HashMap<String, String>() {{
            put("value", "请及时核销");
        }});
        // 可根据实际模板字段调整
        return sendTemplateMessage(openid, templateId, null, data, null);
    }

    /**
     * 内部缓存类
     */
    private static class TokenCache {
        private final String token;
        private final long expireTime;

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