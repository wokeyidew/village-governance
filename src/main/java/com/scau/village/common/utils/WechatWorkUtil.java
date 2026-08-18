package com.scau.village.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * 企业微信机器人工具类
 * 用于发送群机器人消息通知（支持文本、Markdown、图文等）
 *
 * @author system
 * @since 2026-07-18
 */
@Slf4j
@Component
public class WechatWorkUtil {

    @Value("${wechat.work.webhook:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送文本消息
     *
     * @param content 消息内容（支持 @ 成员）
     * @param mentionedList @ 的成员列表（手机号或userid）
     * @return true-发送成功，false-发送失败
     */
    public boolean sendTextMessage(String content, List<String> mentionedList) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("企业微信 Webhook 地址未配置，消息未发送");
            return false;
        }

        JSONObject body = new JSONObject();
        body.put("msgtype", "text");
        JSONObject text = new JSONObject();
        text.put("content", content);
        if (mentionedList != null && !mentionedList.isEmpty()) {
            text.put("mentioned_list", mentionedList);
        }
        body.put("text", text);

        return sendRequest(body);
    }

    /**
     * 发送 Markdown 消息
     *
     * @param content Markdown 格式内容
     * @return true-发送成功，false-发送失败
     */
    public boolean sendMarkdownMessage(String content) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("企业微信 Webhook 地址未配置，消息未发送");
            return false;
        }

        JSONObject body = new JSONObject();
        body.put("msgtype", "markdown");
        JSONObject markdown = new JSONObject();
        markdown.put("content", content);
        body.put("markdown", markdown);

        return sendRequest(body);
    }

    /**
     * 发送图文消息（最多8条）
     *
     * @param articles 图文消息列表（每条包含 title, description, url, picurl）
     * @return true-发送成功，false-发送失败
     */
    public boolean sendNewsMessage(List<JSONObject> articles) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("企业微信 Webhook 地址未配置，消息未发送");
            return false;
        }
        if (articles == null || articles.isEmpty() || articles.size() > 8) {
            log.warn("图文消息条数需在1-8之间，当前条数：{}", articles == null ? 0 : articles.size());
            return false;
        }

        JSONObject body = new JSONObject();
        body.put("msgtype", "news");
        JSONObject news = new JSONObject();
        news.put("articles", articles);
        body.put("news", news);

        return sendRequest(body);
    }

    /**
     * 发送通用请求
     */
    private boolean sendRequest(JSONObject body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject result = JSON.parseObject(response.getBody());
                if (result.getIntValue("errcode") == 0) {
                    log.info("企业微信机器人消息发送成功");
                    return true;
                } else {
                    log.error("企业微信机器人消息发送失败，errcode={}, errmsg={}",
                            result.getIntValue("errcode"), result.getString("errmsg"));
                    return false;
                }
            } else {
                log.error("企业微信机器人消息发送失败，HTTP状态码：{}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("企业微信机器人消息发送异常", e);
            return false;
        }
    }

    /**
     * 便捷方法：发送兑换通知
     *
     * @param userName  用户姓名
     * @param productName 商品名称
     * @param code 核销码
     */
    public boolean sendExchangeNotification(String userName, String productName, String code) {
        String content = String.format(
                "【兑换通知】\n用户：%s\n商品：%s\n核销码：%s\n请管理员及时核销。",
                userName, productName, code
        );
        return sendTextMessage(content, Collections.emptyList());
    }

    /**
     * 便捷方法：发送核销成功通知
     *
     * @param adminName  管理员姓名
     * @param userName  用户姓名
     * @param productName 商品名称
     */
    public boolean sendVerifyNotification(String adminName, String userName, String productName) {
        String content = String.format(
                "【核销成功】\n管理员：%s\n用户：%s\n商品：%s\n已成功核销。",
                adminName, userName, productName
        );
        return sendTextMessage(content, Collections.emptyList());
    }
}