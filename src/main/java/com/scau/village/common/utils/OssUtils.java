package com.scau.village.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class OssUtils {

    @Value("${aliyun.oss.endpoint:}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String ossUrlPrefix;

    /**
     * 图片访问前缀（从配置文件读取，用于本地存储模式）
     * 开发环境：http://localhost:8080
     * 生产环境：https://api.yourdomain.com
     */
    @Value("${image.url-prefix:http://localhost:8080}")
    private String imageUrlPrefix;

    /**
     * 上传文件到 OSS
     * @param file 文件
     * @return 文件完整访问 URL
     */
    public String upload(MultipartFile file) {
        // 如果配置为空，抛出友好提示
        if (endpoint.isEmpty() || accessKeyId.isEmpty() || accessKeySecret.isEmpty() || bucketName.isEmpty()) {
            throw new RuntimeException("OSS配置未设置，请检查 application.yml 中的 aliyun.oss 配置");
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"))
                    + UUID.randomUUID() + ext;
            ossClient.putObject(bucketName, newFileName, file.getInputStream());
            return ossUrlPrefix + newFileName;
        } catch (IOException e) {
            log.error("上传失败", e);
            throw new RuntimeException("文件上传失败");
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 获取图片完整访问 URL（用于本地存储模式）
     * @param relativePath 相对路径，如 /upload/xxx.jpg
     * @return 完整 URL
     */
    public String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // 如果相对路径以 /upload/ 开头，直接拼接
        if (relativePath.startsWith("/upload/")) {
            return imageUrlPrefix + relativePath;
        }
        return imageUrlPrefix + "/upload/" + relativePath;
    }
}