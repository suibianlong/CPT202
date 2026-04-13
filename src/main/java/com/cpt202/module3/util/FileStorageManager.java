package com.cpt202.module3.util;

import com.cpt202.module3.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// file store
@Component
public class FileStorageManager {

    private final Path uploadRootPath;

    public FileStorageManager(@Value("${module3.upload-dir}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile multipartFile, String folderName) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (!FileTypeValidator.isSupported(originalFilename)) {
            throw new AppException(400, "Unsupported file type.");
        }

        try {
            String safeFolderName = (folderName == null || folderName.isBlank()) ? "" : folderName.trim();
            Path targetFolderPath = safeFolderName.isEmpty()
                    ? uploadRootPath
                    : uploadRootPath.resolve(safeFolderName).normalize();

            Files.createDirectories(targetFolderPath);

            String fileExtension = "";
            if (originalFilename != null) {
                int lastDotIndex = originalFilename.lastIndexOf(".");
                if (lastDotIndex >= 0) {
                    fileExtension = originalFilename.substring(lastDotIndex);
                }
            }

            String storedFilename = UUID.randomUUID().toString().replace("-", "") + fileExtension;
            Path targetFilePath = targetFolderPath.resolve(storedFilename);

            multipartFile.transferTo(targetFilePath.toFile());

            if (safeFolderName.isEmpty()) {
                return storedFilename;
            }
            return safeFolderName + "/" + storedFilename;
        } catch (IOException exception) {
            throw new AppException(
                    500,
                    "Failed to store uploaded file.",
                    java.util.List.of(exception.getMessage())
            );
        }
    }
}