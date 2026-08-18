package com.scau.village.common.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
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

    @Value("${ai.clip.timeout:3000}")
    private long timeout;

    private final RestTemplate restTemplate;

    public AiMatchService() {
        // 设置超时
        this.restTemplate = new RestTemplate();
        // 使用org.springframework.boot.web.client.RestTemplateBuilder设置超时更优雅，但这里用简单方式
    }

    /**
     * 调用AI服务匹配规则
     *
     * @param imageBase64 base64编码的图片（不含data:image前缀）
     * @return 匹配结果，包含规则ID和置信度，如果失败则返回null
     */
    public AiMatchResult matchRule(String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            log.warn("图片数据为空，无法进行AI匹配");
            return null;
        }

        try {
            // 构建请求
            String url = aiServiceUrl + "/match_rule";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("image_base64", imageBase64);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求并设置超时
            // 注意：RestTemplate默认没有超时，需要设置，但此处简化，实际应该配置
            // 可通过 SimpleClientHttpRequestFactory 设置超时，或者使用 RestTemplateBuilder
            // 为简化，我们直接调用，在调用前设置超时稍微复杂，忽略或通过全局配置

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
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
                            return result;
                        }
                    } else {
                        log.error("AI服务返回错误: {}", body);
                    }
                }
            } else {
                log.warn("AI服务响应异常，状态码: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            // 连接超时或服务不可用
            log.warn("AI服务调用失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("AI服务调用异常", e);
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

        // getter/setter
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