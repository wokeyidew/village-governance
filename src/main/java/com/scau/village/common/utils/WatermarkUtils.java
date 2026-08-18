package com.scau.village.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 图片水印工具类
 * 用于在评分证据图片上添加水印信息
 * 水印内容：检查批次 + 户主姓名 + 扣分规则 + 拍摄时间
 *
 * @author system
 * @since 2026-08-18
 */
@Slf4j
@Component
public class WatermarkUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 为图片添加水印
     *
     * @param imageBytes     原始图片字节数组
     * @param batchName      检查批次名称
     * @param userName       户主姓名
     * @param ruleName       扣分规则名称
     * @param inspectionTime 检查时间（可为空，默认当前时间）
     * @return 添加水印后的图片字节数组
     * @throws IOException 图片处理异常
     */
    public byte[] addWatermark(byte[] imageBytes,
                               String batchName,
                               String userName,
                               String ruleName,
                               LocalDateTime inspectionTime) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        // 读取原始图片
        BufferedImage originalImage;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            originalImage = ImageIO.read(bais);
        }
        if (originalImage == null) {
            throw new IOException("无法解析图片格式，请确保上传的是有效的图片文件");
        }

        // 获取图片尺寸
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 创建新的图片（带水印）
        BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = watermarkedImage.createGraphics();

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);

        // 构建水印文本
        String timeStr = inspectionTime != null
                ? inspectionTime.format(DATE_TIME_FORMATTER)
                : LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 拼接水印内容（多行）
        String[] lines = {
                "检查批次：" + batchName,
                "户主：" + userName,
                "扣分规则：" + ruleName,
                "拍摄时间：" + timeStr
        };

        // 设置水印字体
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 16);
        g2d.setFont(font);

        // 计算每行高度和总高度
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int totalHeight = lines.length * lineHeight + (lines.length - 1) * 4; // 行间距4px

        // 水印背景区域（右下角）
        int padding = 20;
        int maxWidth = 0;
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }
        int bgWidth = maxWidth + padding * 2;
        int bgHeight = totalHeight + padding * 2;

        // 计算水印位置（右下角，留出边距）
        int x = width - bgWidth - 20;
        int y = height - bgHeight - 20;
        // 确保不超出边界
        if (x < 0) x = 10;
        if (y < 0) y = 10;

        // 绘制半透明背景
        g2d.setColor(new Color(0, 0, 0, 180)); // 黑色半透明
        g2d.fillRoundRect(x, y, bgWidth, bgHeight, 10, 10);

        // 绘制文字（白色）
        g2d.setColor(Color.WHITE);
        int textX = x + padding;
        int textY = y + padding + fm.getAscent();
        for (String line : lines) {
            g2d.drawString(line, textX, textY);
            textY += lineHeight + 4;
        }

        // 释放资源
        g2d.dispose();

        // 输出为字节数组
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 获取原始图片格式（尝试从原始图片获取）
            String formatName = detectImageFormat(originalImage);
            ImageIO.write(watermarkedImage, formatName, baos);
            return baos.toByteArray();
        }
    }

    /**
     * 为 MultipartFile 添加水印
     *
     * @param file           上传的文件
     * @param batchName      检查批次名称
     * @param userName       户主姓名
     * @param ruleName       扣分规则名称
     * @param inspectionTime 检查时间
     * @return 带水印的字节数组
     * @throws IOException 处理异常
     */
    public byte[] addWatermarkToMultipartFile(MultipartFile file,
                                              String batchName,
                                              String userName,
                                              String ruleName,
                                              LocalDateTime inspectionTime) throws IOException {
        return addWatermark(file.getBytes(), batchName, userName, ruleName, inspectionTime);
    }

    /**
     * 检测图片格式
     *
     * @param image BufferedImage
     * @return 格式名称（如 "png", "jpg", "jpeg"）
     */
    private String detectImageFormat(BufferedImage image) {
        // 简单判断：如果图片有透明通道，使用png，否则使用jpg
        if (image.getColorModel().hasAlpha()) {
            return "png";
        } else {
            return "jpg";
        }
    }

    /**
     * 批量添加水印（用于多张图片）
     *
     * @param imageBytesList 原始图片字节数组列表
     * @param batchName      检查批次名称
     * @param userName       户主姓名
     * @param ruleName       扣分规则名称
     * @param inspectionTime 检查时间
     * @return 带水印的字节数组列表
     * @throws IOException 处理异常
     */
    public java.util.List<byte[]> addWatermarkBatch(java.util.List<byte[]> imageBytesList,
                                                    String batchName,
                                                    String userName,
                                                    String ruleName,
                                                    LocalDateTime inspectionTime) throws IOException {
        java.util.List<byte[]> result = new java.util.ArrayList<>();
        for (byte[] bytes : imageBytesList) {
            result.add(addWatermark(bytes, batchName, userName, ruleName, inspectionTime));
        }
        return result;
    }
}