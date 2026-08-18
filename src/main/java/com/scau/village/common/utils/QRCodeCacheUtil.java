package com.scau.village.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * 动态二维码缓存工具类（基于 Redis）
 * 用于存储和验证动态二维码的临时密钥
 *
 * @author system
 * @since 2026-07-19
 */
@Slf4j
@Component
public class QRCodeCacheUtil {

    private static final String QRCODE_PREFIX = "qrcode:";
    private static final Duration DYNAMIC_EXPIRE = Duration.ofSeconds(15); // 动态码有效期15秒（略大于刷新间隔10秒）

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成动态码key（随机唯一标识）
     */
    public String generateDynamicKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 保存动态码（key-value 形式）
     * value 内容为 "fixedCode:type:activityId"，用于验证
     */
    public void saveDynamicCode(String key, String value) {
        try {
            redisTemplate.opsForValue().set(QRCODE_PREFIX + key, value, DYNAMIC_EXPIRE);
            log.debug("动态码已保存，key={}, value={}, 有效期={}秒", key, value, DYNAMIC_EXPIRE.getSeconds());
        } catch (Exception e) {
            log.error("保存动态码失败，key={}, value={}", key, value, e);
            throw new RuntimeException("保存动态码失败：" + e.getMessage(), e);
        }
    }

    /**
     * 验证动态码是否有效
     * @param key 动态码key
     * @param expectedValue 期望的值（用于匹配）
     * @return true-有效，false-无效或已过期
     */
    public boolean validateDynamicCode(String key, String expectedValue) {
        try {
            String value = redisTemplate.opsForValue().get(QRCODE_PREFIX + key);
            if (value == null) {
                log.debug("动态码不存在或已过期，key={}", key);
                return false;
            }
            boolean valid = expectedValue.equals(value);
            if (valid) {
                log.debug("动态码验证成功，key={}", key);
            } else {
                log.debug("动态码验证失败，key={}, 期望值={}, 实际值={}", key, expectedValue, value);
            }
            return valid;
        } catch (Exception e) {
            log.error("验证动态码异常，key={}", key, e);
            return false;
        }
    }

    /**
     * 删除动态码（使用后销毁，防止重复使用）
     */
    public void removeDynamicCode(String key) {
        try {
            Boolean deleted = redisTemplate.delete(QRCODE_PREFIX + key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("动态码已删除，key={}", key);
            }
        } catch (Exception e) {
            log.error("删除动态码失败，key={}", key, e);
        }
    }
}