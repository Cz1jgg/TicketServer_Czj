package com.itticket.util;

import com.itticket.common.BusinessException;
import com.itticket.common.Constants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {

    public String saveFile(MultipartFile file) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + extension;
        
        Path uploadPath = Paths.get(Constants.FILE_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(400, "文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw BusinessException.of(400, "文件名不能为空");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        boolean allowed = false;
        for (String allowedExt : Constants.ALLOWED_FILE_EXTENSIONS) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw BusinessException.of(400, "不支持的文件类型，仅支持jpg、png、pdf、docx、xlsx格式");
        }
        
        if (file.getSize() > Constants.MAX_FILE_SIZE) {
            throw BusinessException.of(400, "文件大小超过限制，最大支持10MB");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}