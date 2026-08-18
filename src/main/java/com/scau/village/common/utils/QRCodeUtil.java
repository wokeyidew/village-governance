package com.scau.village.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 基于 ZXing 库生成二维码图片，返回 Base64 编码字符串
 * 
 * 需要在 pom.xml 中添加以下依赖：
 * <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>core</artifactId>
 *     <version>3.5.1</version>
 * </dependency>
 * <dependency>
 *     <groupId>com.google.zxing</groupId>
 *     <artifactId>javase</artifactId>
 *     <version>3.5.1</version>
 * </dependency>
 *
 * @author system
 * @since 2026-07-19
 */
@Slf4j
public class QRCodeUtil {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    /**
     * 生成二维码图片，返回 Base64 编码的图片数据（可直接用于 image 标签的 src）
     * 
     * @param content 二维码内容（建议不超过 500 字符）
     * @return Base64 图片字符串，如 "data:image/png;base64,iVBORw0KGgo..."
     */
    public static String generateQRCodeBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | IOException e) {
            log.error("生成二维码失败，内容：{}", content, e);
            throw new RuntimeException("生成二维码失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建二维码扫码内容（JSON格式）
     * 包含固定码、动态key、类型、活动ID和时间戳
     * 
     * @param fixedCode  固定码（活动唯一标识）
     * @param dynamicKey 动态码key（可为null，表示固定二维码）
     * @param type       类型：signin-签到 / checkout-签退
     * @param activityId 活动ID
     * @return JSON字符串，如 {"fixed":"xxx","key":"yyy","type":"signin","activityId":1,"ts":1234567890}
     */
    public static String buildQRCodeContent(String fixedCode, String dynamicKey, String type, Integer activityId) {
        // 使用 StringBuilder 拼接 JSON，避免引入额外依赖
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fixed\":\"")
                .append(fixedCode)
                .append("\",\"key\":\"");
        if (dynamicKey != null) {
            sb.append(dynamicKey);
        }
        sb.append("\",\"type\":\"")
                .append(type)
                .append("\",\"activityId\":")
                .append(activityId)
                .append(",\"ts\":")
                .append(System.currentTimeMillis())
                .append("}");
        return sb.toString();
    }
}