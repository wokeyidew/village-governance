package com.scau.village.module.upload.controller;

import com.scau.village.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 * 支持图片等文件上传，采用本地存储
 *
 * @author system
 * @since 2026-07-17
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UploadController {

    // 使用外部绝对路径（jar 包运行时也可写）
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    /**
     * 图片访问前缀（从配置文件读取）
     * 开发环境：http://localhost:8080
     * 生产环境：https://api.yourdomain.com
     */
    @Value("${image.url-prefix:http://localhost:8080}")
    private String imageUrlPrefix;

    /**
     * 图片上传（旧接口，保留兼容）
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    /**
     * 通用文件上传（供注册页等使用）
     */
    @PostMapping("/file/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    /**
     * 上传核心逻辑
     */
    private Result<String> doUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件为空");
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    log.error("创建上传目录失败: {}", UPLOAD_DIR);
                    return Result.error(500, "服务器创建目录失败");
                }
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            File dest = new File(uploadDir, newFileName);
            file.transferTo(dest);

            // 返回完整的可访问 URL（使用配置的 image.url-prefix）
            String accessUrl = imageUrlPrefix + "/upload/" + newFileName;
            log.info("文件上传成功: {}", accessUrl);
            return Result.success(accessUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }
    }
}