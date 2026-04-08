package com.example.ageestimationbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class FileUploadConfig {

    @Value("${app.file.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}")
    private long maxFileSize;

    public FileUploadConfig() {
    }

    public String getUploadDir() {
        String dir = uploadDir;
        File uploadFolder = new File(dir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        return dir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean isValidFileSize(long fileSize) {
        return fileSize <= maxFileSize;
    }
}
