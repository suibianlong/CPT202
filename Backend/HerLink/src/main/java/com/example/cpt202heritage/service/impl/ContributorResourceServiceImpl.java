package com.example.cpt202heritage.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.cpt202heritage.dto.resource.ResourceQueryRequest;
import com.example.cpt202heritage.dto.resource.ResourceSubmitRequest;
import com.example.cpt202heritage.dto.resource.ResourceUpdateRequest;
import com.example.cpt202heritage.entity.Category;
import com.example.cpt202heritage.entity.Resource;
import com.example.cpt202heritage.entity.ResourceSubmission;
import com.example.cpt202heritage.entity.ResourceTag;
import com.example.cpt202heritage.entity.Tag;
import com.example.cpt202heritage.enums.ResourceStatusEnum;
import com.example.cpt202heritage.enums.ResourceTypeEnum;
import com.example.cpt202heritage.exception.AppException;
import com.example.cpt202heritage.mapper.CategoryMapper;
import com.example.cpt202heritage.mapper.ResourceMapper;
import com.example.cpt202heritage.mapper.ResourceSubmissionMapper;
import com.example.cpt202heritage.mapper.ResourceTagMapper;
import com.example.cpt202heritage.mapper.ReviewRecordMapper;
import com.example.cpt202heritage.mapper.TagMapper;
import com.example.cpt202heritage.service.ContributorResourceService;
import com.example.cpt202heritage.util.FileStorageManager;
import com.example.cpt202heritage.util.ResourceStatusValidator;
import com.example.cpt202heritage.util.TagIdNormalizer;
import com.example.cpt202heritage.vo.CategoryTagOptionVO;
import com.example.cpt202heritage.vo.ResourceDetailVO;
import com.example.cpt202heritage.vo.ResourceListItemVO;

@Service
public class ContributorResourceServiceImpl implements ContributorResourceService {

    // operations and certain tools


    private final ResourceMapper resourceMapper;
    private final ResourceSubmissionMapper resourceSubmissionMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final FileStorageManager fileStorageManager;

    public ContributorResourceServiceImpl(ResourceMapper resourceMapper, ResourceSubmissionMapper resourceSubmissionMapper, ReviewRecordMapper reviewRecordMapper, ResourceTagMapper resourceTagMapper, CategoryMapper categoryMapper, TagMapper tagMapper, FileStorageManager fileStorageManager) {
        this.resourceMapper = resourceMapper;
        this.resourceSubmissionMapper = resourceSubmissionMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.resourceTagMapper = resourceTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.fileStorageManager = fileStorageManager;
    }

    @Override
    @Transactional
    public ResourceDetailVO createDraft(Long currentUserId) {

        // Create a new draft resource
        Resource resource = new Resource();
        LocalDateTime now = LocalDateTime.now();
        Category defaultCategory = loadDefaultDraftCategory();

        // Initialize default draft values
        resource.setContributorId(currentUserId);
        resource.setTitle("");
        resource.setDescription("");
        resource.setCategoryId(defaultCategory.getCategoryId());
        resource.setResourceType(resolveDraftResourceType(defaultCategory));
        resource.setStatus(ResourceStatusEnum.DRAFT.getValue());
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);

        // Insert draft into database
        resourceMapper.insert(resource);

        // Return the draft detail
        return buildResourceDetailVO(resource);
    }

    @Override
    @Transactional
    public ResourceDetailVO updateResource(Long currentUserId, Long resourceId, ResourceUpdateRequest request) {

        // Load resource and check ownership
        Resource resource = loadOwnedResource(currentUserId, resourceId, true);

        // Check whether current status allows editing
        validateEditableStatus(resource);

        if (request == null) {
            throw AppException.badRequest("Update request cannot be null.");
        }

        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            resource.setCategoryId(request.getCategoryId());
        }
        if (request.getPlace() != null) {
            resource.setPlace(request.getPlace());
        }
        if (request.getResourceType() != null) {
            resource.setResourceType(request.getResourceType());
        }

        // Update timestamp and save changes
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(resource);

        // Replace tag relations if tag IDs are provided
        if (request.getTagIds() != null) {
            replaceResourceTags(resourceId, request.getTagIds());
        }

        return buildResourceDetailVO(resource);
    }

    @Override
    @Transactional
    public ResourceDetailVO uploadFiles(Long currentUserId, Long resourceId, MultipartFile previewImage, MultipartFile mediaFile) {

        // Load resource and check edit permission
        Resource resource = loadOwnedResource(currentUserId, resourceId, true);
        validateEditableStatus(resource);

        boolean hasPreviewImage = previewImage != null && !previewImage.isEmpty();
        boolean hasMediaFile = mediaFile != null && !mediaFile.isEmpty();

        // At least one file must be uploaded
        if (!hasPreviewImage && !hasMediaFile) {
            throw AppException.badRequest("At least one file must be uploaded.");
        }

        // Store files under a resource-specific folder
        String folderName = "resource-" + resourceId;


        // Save preview image if provided
        if (hasPreviewImage) {
            String previewImagePath = fileStorageManager.store(previewImage, folderName);
            resource.setPreviewImage(previewImagePath);
        }


        // Save main media file if provided
        if (hasMediaFile) {
            String mediaFilePath = fileStorageManager.store(mediaFile, folderName);
            resource.setMediaUrl(mediaFilePath);
        }


        // Update timestamp and save file paths
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.updateById(resource);

        return buildResourceDetailVO(resource);
    }

    @Override
    public List<ResourceListItemVO> listMyResources(Long currentUserId, ResourceQueryRequest request) {
        String keyword = null;
        String status = null;
        Long categoryId = null;

        if (request != null) {
            keyword = request.getKeyword();
            status = request.getStatus();
            categoryId = request.getCategoryId();
        }

        List<Resource> resourceList = resourceMapper.selectMyResources(
                currentUserId,
                keyword,
                status,
                categoryId
        );

        if (resourceList == null) {
            resourceList = Collections.emptyList();
        }

        List<ResourceListItemVO> resourceListItemVOList = new ArrayList<>();

        for (Resource resource : resourceList) {
            ResourceListItemVO resourceListItemVO = new ResourceListItemVO();
            resourceListItemVO.setId(resource.getId());
            resourceListItemVO.setTitle(resource.getTitle());
            resourceListItemVO.setPreviewImage(resource.getPreviewImage());
            resourceListItemVO.setStatus(resource.getStatus());
            resourceListItemVO.setResourceType(resource.getResourceType());
            resourceListItemVO.setCategoryId(resource.getCategoryId());
            resourceListItemVO.setUpdatedAt(resource.getUpdatedAt());
            resourceListItemVOList.add(resourceListItemVO);
        }

        return resourceListItemVOList;
    }

    @Override
    public ResourceDetailVO getMyResourceDetail(Long currentUserId, Long resourceId) {
        Resource resource = loadOwnedResource(currentUserId, resourceId);
        return buildResourceDetailVO(resource);
    }

    @Override
    @Transactional
    public void submitResource(Long currentUserId, Long resourceId, ResourceSubmitRequest request) {

        // Load resource and check whether it can be submitted
        Resource resource = loadOwnedResource(currentUserId, resourceId, true);
        validateSubmittableResource(resource);

        // Calculate the next submission version number
        ResourceSubmission latestSubmission = resourceSubmissionMapper.selectLatestByResourceId(resourceId);
        int nextVersionNo = latestSubmission == null || latestSubmission.getVersionNo() == null
                ? 1
                : latestSubmission.getVersionNo() + 1;

        LocalDateTime now = LocalDateTime.now();

        // Create a new submission record
        ResourceSubmission resourceSubmission = new ResourceSubmission();
        resourceSubmission.setResourceId(resourceId);
        resourceSubmission.setVersionNo(nextVersionNo);
        resourceSubmission.setSubmittedBy(currentUserId);
        resourceSubmission.setSubmittedAt(now);
        resourceSubmission.setCreatedAt(now);

        // Save submission note if provided
        if (request != null) {
            resourceSubmission.setSubmissionNote(request.getSubmissionNote());
        }

        // Insert submission history
        resourceSubmission.setStatusSnapshot(ResourceStatusEnum.PENDING_REVIEW.getValue());
        resourceSubmissionMapper.insert(resourceSubmission);

        // Update the main resource status
        resource.setStatus(ResourceStatusEnum.PENDING_REVIEW.getValue());
        resource.setUpdatedAt(now);
        resourceMapper.updateById(resource);
    }

    @Override
    public List<CategoryTagOptionVO> listCategoryOptions() {
        List<Category> categoryList = categoryMapper.selectActiveCategories();
        if (categoryList == null) {
            categoryList = Collections.emptyList();
        }
        List<CategoryTagOptionVO> optionVOList = new ArrayList<>();

        for (Category category : categoryList) {
            CategoryTagOptionVO optionVO = new CategoryTagOptionVO();
            optionVO.setId(category.getCategoryId());
            optionVO.setName(category.getCategoryTopic());
            optionVOList.add(optionVO);
        }

        return optionVOList;
    }

    @Override
    public List<CategoryTagOptionVO> listTagOptions() {
        List<Tag> tagList = tagMapper.selectActiveTags();
        if (tagList == null) {
            tagList = Collections.emptyList();
        }
        List<CategoryTagOptionVO> optionVOList = new ArrayList<>();

        for (Tag tag : tagList) {
            CategoryTagOptionVO optionVO = new CategoryTagOptionVO();
            optionVO.setId(tag.getTagId());
            optionVO.setName(tag.getTagName());
            optionVOList.add(optionVO);
        }

        return optionVOList;
    }

    private Resource loadOwnedResource(Long currentUserId, Long resourceId) {
        return loadOwnedResource(currentUserId, resourceId, false);
    }

    private Resource loadOwnedResource(Long currentUserId, Long resourceId, boolean forUpdate) {
        // Use row lock when the caller is going to modify the resource
        Resource resource = forUpdate
                ? resourceMapper.selectByIdForUpdate(resourceId)
                : resourceMapper.selectById(resourceId);

        // Throw if resource does not exist
        if (resource == null) {
            throw AppException.notFound("Resource does not exist.");
        }

        // Throw if the current user is not the owner
        if (!Objects.equals(resource.getContributorId(), currentUserId)) {
            throw AppException.forbidden("Current user does not own this resource.");
        }

        return resource;
    }

    private Category loadDefaultDraftCategory() {
        List<Category> categoryList = categoryMapper.selectActiveCategories();
        if (categoryList == null || categoryList.isEmpty()) {
            throw AppException.conflict("Cannot create draft because no active category is available.");
        }

        Category defaultCategory = categoryList.get(0);
        if (defaultCategory.getCategoryId() == null) {
            throw AppException.conflict("Cannot create draft because the default category is invalid.");
        }

        return defaultCategory;
    }

    private String resolveDraftResourceType(Category defaultCategory) {
        String categoryType = defaultCategory.getCategoryType();
        if (categoryType != null && !categoryType.isBlank()) {
            try {
                return ResourceTypeEnum.fromValue(categoryType).getValue();
            } catch (IllegalArgumentException ignored) {
            }
        }

        return ResourceTypeEnum.IMAGE.getValue();
    }

    private void validateEditableStatus(Resource resource) {
        ResourceStatusValidator.assertEditable(resource);
    }

    private void validateSubmittableResource(Resource resource) {
        ResourceStatusValidator.assertSubmittable(resource);

        if (resource.getTitle() == null || resource.getTitle().isBlank()) {
            throw AppException.badRequest("Title is required.");
        }

        if (resource.getDescription() == null || resource.getDescription().isBlank()) {
            throw AppException.badRequest("Description is required.");
        }

        if (resource.getCategoryId() == null) {
            throw AppException.badRequest("Category is required.");
        }

        if (resource.getResourceType() == null || resource.getResourceType().isBlank()) {
            throw AppException.badRequest("Resource type is required.");
        }

        if (resource.getMediaUrl() == null || resource.getMediaUrl().isBlank()) {
            throw AppException.badRequest("Media file is required.");
        }
    }

    private void replaceResourceTags(Long resourceId, List<Long> tagIds) {
        // Remove old tag relations
        resourceTagMapper.deleteByResourceId(resourceId);

        if (tagIds.isEmpty()) {
            return;
        }

        // Remove null values and duplicates
        List<Long> distinctTagIds = TagIdNormalizer.distinctNonNull(tagIds);


        // Insert new tag relations
        for (Long tagId : distinctTagIds) {
            ResourceTag resourceTag = new ResourceTag();
            resourceTag.setResourceId(resourceId);
            resourceTag.setTagId(tagId);
            resourceTagMapper.insert(resourceTag);
        }
    }

    private ResourceDetailVO buildResourceDetailVO(Resource resource) {


        // Convert entity data into detail VO
        ResourceDetailVO resourceDetailVO = new ResourceDetailVO();

        resourceDetailVO.setId(resource.getId());
        resourceDetailVO.setContributorId(resource.getContributorId());
        resourceDetailVO.setTitle(resource.getTitle());
        resourceDetailVO.setDescription(resource.getDescription());
        resourceDetailVO.setCategoryId(resource.getCategoryId());
        resourceDetailVO.setPlace(resource.getPlace());
        resourceDetailVO.setPreviewImage(resource.getPreviewImage());
        resourceDetailVO.setMediaUrl(resource.getMediaUrl());
        resourceDetailVO.setStatus(resource.getStatus());
        resourceDetailVO.setReviewedAt(resource.getReviewedAt());
        resourceDetailVO.setCreatedAt(resource.getCreatedAt());
        resourceDetailVO.setUpdatedAt(resource.getUpdatedAt());
        resourceDetailVO.setArchivedAt(resource.getArchivedAt());
        resourceDetailVO.setResourceType(resource.getResourceType());


        // Load related tag IDs for the detail view
        List<Long> tagIds = resourceTagMapper.selectTagIdsByResourceId(resource.getId());
        resourceDetailVO.setTagIds(tagIds);

        return resourceDetailVO;
    }
}
