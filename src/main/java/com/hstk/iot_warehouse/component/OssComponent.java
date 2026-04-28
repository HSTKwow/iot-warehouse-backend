package com.hstk.iot_warehouse.component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class OssComponent {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 OSS
     * @param file 前端上传的文件
     * @return 文件的访问URL
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 1. 构建存储路径: material_images/2026/2/15/uuid.jpg
        String originalFilename = file.getOriginalFilename();
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成唯一文件名，避免冲突
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = "material_images/" + datePath + "/" + fileName;

        // 2. 创建 OSS Client
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 上传
            ossClient.putObject(bucketName, objectName, inputStream);
            
            // 3. 拼接返回 URL
            // URL 格式: https://bucketName.endpoint/objectName
            // 注意: endpoint 中可能已经包含了 https://
            String urlEndpoint = endpoint.replace("http://", "").replace("https://", "");
            String url = "https://" + bucketName + "." + urlEndpoint + "/" + objectName;
            
            log.info("File uploaded successfully to OSS: {}", url);
            return url;
            
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除OSS文件
     * @param fileUrl 文件的完整URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // URL 示例: https://bucket-name.oss-cn-hangzhou.aliyuncs.com/material_images/2023/10/24/uuid.jpg
            // 需要提取 objectName: material_images/2023/10/24/uuid.jpg
            
            // 简单解析：找到 .com/ 之后的部分
            String domain = "aliyuncs.com/";
            int index = fileUrl.indexOf(domain);
            if (index == -1) {
                log.warn("Invalid OSS URL format, cannot delete: {}", fileUrl);
                return;
            }
            
            String objectName = fileUrl.substring(index + domain.length());
            
            // 创建 OSSClient 实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            
            try {
                // 删除文件
                ossClient.deleteObject(bucketName, objectName);
                log.info("Deleted OSS file: {}", objectName);
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete OSS file: {}", fileUrl, e);
            // 不抛出异常，避免阻断主业务流程
        }
    }
}
