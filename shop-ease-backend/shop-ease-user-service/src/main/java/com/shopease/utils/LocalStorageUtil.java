package com.shopease.utils;

import com.shopease.config.LocalStorageConfig;
import com.shopease.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件上传工具类（适配配置化目录）
 * @author hspcadmin
 */
@Component
public class LocalStorageUtil {

    @Resource
    private LocalStorageConfig localStorageConfig;

    /**
     * 上传文件到本地服务器
     * @param file 上传的文件（如头像）
     * @param module 模块名（如 "avatar" 区分头像，"goods" 区分商品图片）
     * @return 前端可访问的完整URL（如：/upload/avatar/20251203/xxx.png）
     */
    public String upload(MultipartFile file, String module) {
        // 1. 校验本地存储是否启用
        if (!localStorageConfig.isEnabled()) {
            throw new BusinessException("本地存储未启用，请检查配置");
        }

        // 2. 校验文件合法性
        validateFile(file);

        // 3. 生成动态路径（根目录/模块名/日期目录/文件名）
        String originalFilename = file.getOriginalFilename();
        // 文件后缀（如 png）
        String suffix = StringUtils.getFilenameExtension(originalFilename);
        // 生成唯一文件名（避免覆盖）
        String fileName = UUID.randomUUID() + "." + suffix;
        // 按日期分目录（方便管理）
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 4. 拼接存储路径（根目录/模块名/日期目录）
        String storageDirPath = localStorageConfig.getRootDir() + module + File.separator + dateDir;
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // 目录不存在则自动创建（包括多级目录）
        }

        // 5. 写入文件到本地
        File targetFile = new File(storageDir, fileName);
        try {
            // 高效写入文件
            FileCopyUtils.copy(file.getBytes(), targetFile);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }

        // 6. 拼接前端可访问的URL（访问前缀/模块名/日期目录/文件名）
        return localStorageConfig.getAccessPrefix() + module + "/" + dateDir + "/" + fileName;
    }

    /**
     * 校验文件大小和类型
     */
    private void validateFile(MultipartFile file) {
        // 校验文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 校验文件大小
        if (file.getSize() > localStorageConfig.getMaxFileSize()) {
            long maxSizeMB = localStorageConfig.getMaxFileSize() / 1024 / 1024;
            throw new BusinessException("文件大小不能超过 " + maxSizeMB + "MB");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (!localStorageConfig.getAllowTypes().contains(contentType)) {
            throw new BusinessException("不支持的文件类型，仅允许：" + String.join(",", localStorageConfig.getAllowTypes()));
        }
    }

    /**
     * （可选）删除本地文件（如用户更换头像时删除旧文件）
     * @param accessUrl 前端访问的URL（如：/upload/avatar/20251203/xxx.png）
     */
    public void deleteFile(String accessUrl) {
        if (!localStorageConfig.isEnabled() || !StringUtils.hasText(accessUrl)) {
            return;
        }

        // 从URL中提取存储路径（去掉访问前缀）
        String relativePath = accessUrl.replace(localStorageConfig.getAccessPrefix(), "");
        String filePath = localStorageConfig.getRootDir() + relativePath;

        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete(); // 删除文件
        }
    }
}
