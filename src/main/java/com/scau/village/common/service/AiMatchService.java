package com.scau.village.common.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI匹配服务
 * 调用Python CLIP服务，根据图片匹配最合适的积分规则
 *
 * @author system
 * @since 2026-08-19
 */
@Slf4j
@Service
public class AiMatchService {

    @Value("${ai.clip.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${ai.clip.timeout:5000}")
    private int timeout;

    private final RestTemplate restTemplate;

    public AiMatchService() {
        // 配置超时（连接超时 + 读取超时），CLIP 推理在 CPU 上可能需要较长时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.restTemplate = new RestTemplate(factory);
        log.info("【AI服务】初始化完成，超时时间: {}ms", timeout);
    }

    /**
     * 调用AI服务匹配规则
     *
     * @param imageBase64 base64编码的图片（可含 data:image 前缀）
     * @return 匹配结果，包含规则ID和置信度，如果失败则返回null
     */
    public AiMatchResult matchRule(String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            log.warn("【AI服务】图片数据为空，无法进行匹配");
            return null;
        }

        // 去除 data:image 前缀（如果存在）
        String cleanBase64 = imageBase64;
        if (cleanBase64.contains(",")) {
            cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
        }

        long startTime = System.currentTimeMillis();
        log.info("【AI服务】开始调用，URL: {}, 图片长度: {}", aiServiceUrl + "/match_rule", cleanBase64.length());

        try {
            String url = aiServiceUrl + "/match_rule";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image_base64", cleanBase64);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("【AI服务】调用完成，状态码: {}, 耗时: {}ms", response.getStatusCode(), elapsed);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                log.debug("【AI服务】响应内容: {}", body);
                if (body != null) {
                    JSONObject json = JSONObject.parseObject(body);
                    if (json.containsKey("code") && json.getInteger("code") == 200) {
                        JSONObject data = json.getJSONObject("data");
                        if (data != null) {
                            AiMatchResult result = new AiMatchResult();
                            result.setRuleIndex(data.getInteger("rule_index"));
                            result.setRuleName(data.getString("matched_rule"));
                            result.setConfidence(data.getDouble("confidence"));
                            result.setSuggestedAction(data.getString("suggested_action"));
                            result.setSuggestedPoints(data.getInteger("suggested_points"));
                            log.info("【AI服务】匹配成功: ruleIndex={}, confidence={}", 
                                    result.getRuleIndex(), result.getConfidence());
                            return result;
                        }
                    } else {
                        log.error("【AI服务】返回错误: {}", body);
                    }
                }
            } else {
                log.warn("【AI服务】响应异常，状态码: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("【AI服务】调用失败（超时或服务不可用），耗时: {}ms, 错误: {}", elapsed, e.getMessage());
        } catch (Exception e) {
            log.error("【AI服务】调用异常", e);
        }

        // 降级：返回null，由上层处理
        return null;
    }

    /**
     * AI匹配结果封装
     */
    public static class AiMatchResult {
        private Integer ruleIndex;          // 匹配的规则ID或序号
        private String ruleName;            // 规则描述文本
        private Double confidence;          // 置信度 0-1
        private String suggestedAction;     // 建议操作：加分/扣分
        private Integer suggestedPoints;    // 建议分数

        public Integer getRuleIndex() { return ruleIndex; }
        public void setRuleIndex(Integer ruleIndex) { this.ruleIndex = ruleIndex; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getSuggestedAction() { return suggestedAction; }
        public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
        public Integer getSuggestedPoints() { return suggestedPoints; }
        public void setSuggestedPoints(Integer suggestedPoints) { this.suggestedPoints = suggestedPoints; }
    }
}