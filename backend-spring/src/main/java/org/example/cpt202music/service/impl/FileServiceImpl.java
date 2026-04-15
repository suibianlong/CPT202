package org.example.cpt202music.service.impl;

import org.example.cpt202music.model.dto.AttachedFileDTO;
import org.example.cpt202music.model.entity.AttachedFile;
import org.example.cpt202music.model.entity.Resource;
import org.example.cpt202music.repository.AttachedFileRepository;
import org.example.cpt202music.repository.ResourceRepository;
import org.example.cpt202music.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AttachedFileRepository attachedFileRepository;
    private final ResourceRepository resourceRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ==================== 文件上传接口 ====================
    // 上传文件到指定资源，并更新资源的最后更新时间
    // 数据流：接收文件 → 生成存储文件名 → 保存到文件系统 → 保存文件元数据到数据库

    @Override
    @Transactional
    public AttachedFileDTO uploadFile(Long resourceId, MultipartFile file) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        try {
            String originalFilename = file.getOriginalFilename();
            String storedFilename = generateStoredFilename(originalFilename);
            Path uploadPath = Paths.get(uploadDir, resourceId.toString());

            // 确保上传目录存在
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件到指定路径
            Path filePath = uploadPath.resolve(storedFilename);
            file.transferTo(filePath.toFile());

            // 创建文件元数据并保存
            AttachedFile attachedFile = new AttachedFile();
            attachedFile.setResource(resource);
            attachedFile.setOriginalFilename(originalFilename);
            attachedFile.setStoredFilename(storedFilename);
            attachedFile.setFilePath(filePath.toString());
            attachedFile.setFileType(file.getContentType());
            attachedFile.setFileSize(file.getSize());
            attachedFile.setUploadedAt(LocalDateTime.now());

            AttachedFile savedFile = attachedFileRepository.save(attachedFile);

            // 更新资源的最后更新时间
            resource.setLastUpdatedTime(LocalDateTime.now());
            resourceRepository.save(resource);

            return toDTO(savedFile);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    // ==================== 文件查询接口 ====================
    // 根据资源 ID 查询所有附件文件列表

    @Override
    public List<AttachedFileDTO> getFilesByResourceId(Long resourceId) {
        return attachedFileRepository.findByResourceResourceId(resourceId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== 文件删除接口 ====================
    // 删除指定的附件文件，同时清理文件系统和数据库记录

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long resourceId) {
        AttachedFile file = attachedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getResource().getResourceId().equals(resourceId)) {
            throw new RuntimeException("File not found");
        }

        // 删除文件系统中的文件
        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 文件不存在时忽略错误
        }

        // 删除数据库记录
        attachedFileRepository.delete(file);

        // 更新资源的最后更新时间
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setLastUpdatedTime(LocalDateTime.now());
        resourceRepository.save(resource);
    }

    // ==================== 工具方法 ====================
    // 生成唯一的存储文件名，使用时间戳和 UUID 前缀避免文件名冲突

    private String generateStoredFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 9) + extension;
    }

    // 将 AttachedFile 实体转换为 DTO
    private AttachedFileDTO toDTO(AttachedFile file) {
        AttachedFileDTO dto = new AttachedFileDTO();
        dto.setFileId(file.getFileId());
        dto.setOriginalFilename(file.getOriginalFilename());
        dto.setStoredFilename(file.getStoredFilename());
        dto.setFileType(file.getFileType());
        dto.setFileSize(file.getFileSize());
        dto.setUploadedAt(file.getUploadedAt() != null ? file.getUploadedAt().toString() : null);
        return dto;
    }
}
