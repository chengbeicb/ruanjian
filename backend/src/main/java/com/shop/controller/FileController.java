package com.shop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileController {

    // 上传文件存储路径
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            System.out.println("上传目录路径: " + uploadDir.getAbsolutePath());
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("创建上传目录: " + (created ? "成功" : "失败"));
            }
            
            // 检查目录权限
            System.out.println("目录可读: " + uploadDir.canRead());
            System.out.println("目录可写: " + uploadDir.canWrite());
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
            System.out.println("保存文件路径: " + filePath.toAbsolutePath());
            Files.write(filePath, file.getBytes());
            
            // 验证文件是否成功保存
            File savedFile = filePath.toFile();
            System.out.println("文件保存成功: " + savedFile.exists());
            System.out.println("文件大小: " + savedFile.length() + " bytes");
            
            // 构建文件访问URL
            // 确保URL使用正斜杠，这对Web访问更安全
            String fileUrl = "/uploads/" + uniqueFilename;
            
            // 返回成功响应 - 将fileUrl改为url以匹配前端期望的字段名
            response.put("success", true);
            response.put("url", fileUrl);  // 这里改为url而不是fileUrl
            response.put("filename", uniqueFilename);
            response.put("message", "文件上传成功");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件上传异常: " + e.getMessage());
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}