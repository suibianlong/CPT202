package com.example.cpt202heritage.service.impl;

import com.example.cpt202heritage.dto.*;
import com.example.cpt202heritage.entity.*;
import com.example.cpt202heritage.repository.*;
import com.example.cpt202heritage.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final AttachedFileRepository attachedFileRepository;

    @Override
    @Transactional
    public ResourceResponseDTO createResource(Long userId, ResourceCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Resource resource = new Resource();
        resource.setContributor(user);
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setPlace(dto.getPlace());
        resource.setStatus(Resource.ResourceStatus.DRAFT);

        // 设置分类和标签
        updateResourceCategoriesAndTags(resource, dto);

        Resource savedResource = resourceRepository.save(resource);
        return toResponseDTO(savedResource);
    }

    @Override
    public List<ResourceResponseDTO> getMyResources(Long userId, String status) {
        List<Resource> resources;

        if (status != null && !status.isEmpty()) {
            Resource.ResourceStatus resourceStatus = Resource.ResourceStatus.valueOf(status.toUpperCase().replace(" ", "_"));
            resources = resourceRepository.findByContributorIdAndStatusOrderByLastUpdatedTimeDesc(userId, resourceStatus);
        } else {
            resources = resourceRepository.findByContributorIdOrderByLastUpdatedTimeDesc(userId);
        }

        return resources.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResourceResponseDTO getResourceById(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findByIdWithCategoriesAndTags(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getContributor().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return toResponseDTO(resource);
    }

    @Override
    @Transactional
    public ResourceResponseDTO updateResource(Long resourceId, Long userId, ResourceCreateDTO dto) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getContributor().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (resource.getStatus() != Resource.ResourceStatus.DRAFT &&
            resource.getStatus() != Resource.ResourceStatus.REJECTED) {
            throw new RuntimeException("Cannot edit in this status");
        }

        // 更新资源基本信息
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setPlace(dto.getPlace());
        resource.setLastUpdatedTime(LocalDateTime.now());

        // 更新分类和标签
        updateResourceCategoriesAndTags(resource, dto);

        Resource updatedResource = resourceRepository.save(resource);
        return toResponseDTO(updatedResource);
    }

    @Override
    @Transactional
    public ResourceResponseDTO saveDraft(Long resourceId, Long userId, ResourceCreateDTO dto) {
        if (resourceId == null) {
            return createResource(userId, dto);
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getContributor().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // 更新资源基本信息
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setPlace(dto.getPlace());
        resource.setLastUpdatedTime(LocalDateTime.now());

        // 更新分类和标签
        updateResourceCategoriesAndTags(resource, dto);

        Resource savedResource = resourceRepository.save(resource);
        return toResponseDTO(savedResource);
    }

    // ==================== 资源提交审核接口 ====================
    // 提交资源进行审核
    // 前提条件：资源状态必须是 DRAFT 或 REJECTED，且至少上传了 1 个附件文件

    @Override
    @Transactional
    public ResourceResponseDTO submitForReview(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getContributor().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (resource.getStatus() != Resource.ResourceStatus.DRAFT &&
            resource.getStatus() != Resource.ResourceStatus.REJECTED) {
            throw new RuntimeException("Cannot submit in this status");
        }

        long fileCount = attachedFileRepository.countByResourceResourceId(resourceId);
        if (fileCount == 0) {
            throw new RuntimeException("Please upload at least 1 file before submitting");
        }

        resource.setStatus(Resource.ResourceStatus.PENDING_REVIEW);
        resource.setLastSubmittedTime(LocalDateTime.now());
        resource.setLastUpdatedTime(LocalDateTime.now());

        Resource updatedResource = resourceRepository.save(resource);
        return toResponseDTO(updatedResource);
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getContributor().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (resource.getStatus() != Resource.ResourceStatus.DRAFT) {
            throw new RuntimeException("Can only delete draft resources");
        }

        resourceRepository.delete(resource);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertTagToDTO)
                .collect(Collectors.toList());
    }

    // ==================== DTO 转换方法 ====================
    // 将 Resource 实体转换为响应 DTO，包含关联的分类、标签和附件文件信息

    private ResourceResponseDTO toResponseDTO(Resource resource) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setResourceId(resource.getResourceId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setPlace(resource.getPlace());
        dto.setStatus(resource.getStatus().name().replace("_", " "));
        dto.setCreatedTime(formatDateTime(resource.getCreatedTime()));
        dto.setLastUpdatedTime(formatDateTime(resource.getLastUpdatedTime()));
        dto.setLastSubmittedTime(formatDateTime(resource.getLastSubmittedTime()));
        dto.setLastPublishedTime(formatDateTime(resource.getLastPublishedTime()));

        // 转换关联的分类信息
        if (resource.getCategories() != null) {
            dto.setCategories(resource.getCategories().stream()
                    .map(this::convertCategoryToDTO)
                    .collect(Collectors.toList()));
        }

        // 转换关联的标签信息
        if (resource.getTags() != null) {
            dto.setTags(resource.getTags().stream()
                    .map(this::convertTagToDTO)
                    .collect(Collectors.toList()));
        }

        // 转换关联的附件文件信息
        if (resource.getAttachedFiles() != null) {
            dto.setAttachedFiles(resource.getAttachedFiles().stream()
                    .map(this::convertAttachedFileToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // 将 Category 实体转换为 DTO，用于列表展示
    private CategoryDTO convertCategoryToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryType(category.getCategoryType());
        dto.setCategoryTopic(category.getCategoryTopic());
        return dto;
    }

    // 将 Tag 实体转换为 DTO，用于列表展示
    private TagDTO convertTagToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setTagId(tag.getTagId());
        dto.setTagName(tag.getTagName());
        return dto;
    }

    // 将 AttachedFile 实体转换为 DTO，包含文件元数据
    private AttachedFileDTO convertAttachedFileToDTO(AttachedFile file) {
        AttachedFileDTO dto = new AttachedFileDTO();
        dto.setFileId(file.getFileId());
        dto.setOriginalFilename(file.getOriginalFilename());
        dto.setFileType(file.getFileType());
        dto.setFileSize(file.getFileSize());
        dto.setUploadedAt(formatDateTime(file.getUploadedAt()));
        return dto;
    }

    // 日期时间格式化工具方法，将 LocalDateTime 转为字符串
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }

    // ==================== 私有工具方法 ====================
    // 统一处理资源的分类和标签更新逻辑
    // 如果 categoryIds 为空或 null，清空分类；否则根据 ID 列表设置分类
    // 如果 tagNames 为空或 null，清空标签；否则根据标签名称查找或创建标签

    private void updateResourceCategoriesAndTags(Resource resource, ResourceCreateDTO dto) {
        // 处理分类
        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
            resource.setCategories(new HashSet<>());
        } else {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            resource.setCategories(new HashSet<>(categories));
        }

        // 处理标签：如果标签不存在则自动创建
        if (dto.getTagNames() == null || dto.getTagNames().isEmpty()) {
            resource.setTags(new HashSet<>());
        } else {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : dto.getTagNames()) {
                Tag tag = tagRepository.findByTagName(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setTagName(tagName.trim());
                            newTag.setStatus(Tag.TagStatus.ACTIVE);
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            resource.setTags(tags);
        }
    }
}
