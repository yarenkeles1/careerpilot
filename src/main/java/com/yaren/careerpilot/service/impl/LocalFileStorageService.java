package com.yaren.careerpilot.service.impl;

import com.yaren.careerpilot.exception.FileStorageException;
import com.yaren.careerpilot.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    public String store(MultipartFile file) {

        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);

        String extension = getFileExtension(file.getOriginalFilename());

        String uniqueFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadPath.resolve(uniqueFileName);

        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            throw new FileStorageException("Failed to save resume.", e);
        }
    }

    @Override
    public void delete(String filePath) {

        Path path = Paths.get(filePath);

        try {
            Files.deleteIfExists(path);

        } catch (IOException e) {
            throw new FileStorageException("Failed to delete resume.", e);
        }
    }

    private String getFileExtension(String fileName) {

        int lastDot = fileName.lastIndexOf(".");

        if (lastDot == -1) {
            throw new IllegalArgumentException("File extension is missing.");
        }

        return fileName.substring(lastDot);
    }
}
