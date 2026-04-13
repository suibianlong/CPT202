package com.cpt202.module3.service;

import com.cpt202.module3.mapper.CategoryMapper;
import com.cpt202.module3.mapper.ResourceMapper;
import com.cpt202.module3.mapper.ResourceSubmissionMapper;
import com.cpt202.module3.mapper.ResourceTagMapper;
import com.cpt202.module3.mapper.ReviewRecordMapper;
import com.cpt202.module3.mapper.TagMapper;
import com.cpt202.module3.service.impl.ContributorResourceServiceImpl;
import com.cpt202.module3.util.FileStorageManager;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cpt202.module3.entity.Category;
import com.cpt202.module3.entity.Resource;
import com.cpt202.module3.enums.ResourceStatusEnum;
import com.cpt202.module3.vo.ResourceDetailVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import com.cpt202.module3.dto.resource.ResourceUpdateRequest;
import com.cpt202.module3.entity.ResourceTag;
import com.cpt202.module3.dto.resource.ResourceSubmitRequest;
import com.cpt202.module3.exception.AppException;
import com.cpt202.module3.entity.ResourceSubmission;



@ExtendWith(MockitoExtension.class)
class ContributorResourceServiceImplTest {

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private ResourceSubmissionMapper resourceSubmissionMapper;

    @Mock
    private ReviewRecordMapper reviewRecordMapper;

    @Mock
    private ResourceTagMapper resourceTagMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private FileStorageManager fileStorageManager;

    @InjectMocks
    private ContributorResourceServiceImpl contributorResourceService;

    @Test
    void createDraft_shouldInsertDraftAndReturnDetailVO() {
        // setup
        Category category = new Category();
        category.setCategoryId(1L);
        category.setCategoryType("IMAGE");
        category.setCategoryTopic("Object");

        when(categoryMapper.selectActiveCategories()).thenReturn(java.util.List.of(category));

        Long currentUserId = 1L;

        doAnswer(invocation -> {
            Resource resource = invocation.getArgument(0);
            resource.setId(100L);
            return 1;
        }).when(resourceMapper).insert(any(Resource.class));

        when(resourceTagMapper.selectTagIdsByResourceId(100L)).thenReturn(java.util.List.of());

        // call
        ResourceDetailVO result = contributorResourceService.createDraft(currentUserId);

        // assertion
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(currentUserId, result.getContributorId());
        assertEquals(ResourceStatusEnum.DRAFT.getValue(), result.getStatus());
        assertEquals(1L, result.getCategoryId());

        ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceMapper).insert(captor.capture());

        Resource inserted = captor.getValue();
        assertEquals(currentUserId, inserted.getContributorId());
        assertEquals(ResourceStatusEnum.DRAFT.getValue(), inserted.getStatus());
        assertEquals(1L, inserted.getCategoryId());
        assertNotNull(inserted.getCreatedAt());
        assertNotNull(inserted.getUpdatedAt());
    }

    @Test
    void updateResource_shouldUpdateMetadataAndReplaceTags() {
        // setup
        Long currentUserId = 1L;
        Long resourceId = 10L;

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setContributorId(currentUserId);
        resource.setStatus(ResourceStatusEnum.DRAFT.getValue());

        when(resourceMapper.selectByIdForUpdate(resourceId)).thenReturn(resource);
        when(resourceTagMapper.selectTagIdsByResourceId(resourceId)).thenReturn(java.util.List.of(11L, 12L));

        ResourceUpdateRequest request = new ResourceUpdateRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setCategoryId(2L);
        request.setPlace("Suzhou");
        request.setResourceType("IMAGE");
        request.setTagIds(new java.util.ArrayList<>(java.util.Arrays.asList(11L, 11L, null, 12L)));

        // call
        ResourceDetailVO result = contributorResourceService.updateResource(currentUserId, resourceId, request);

        // assertion
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals(2L, result.getCategoryId());
        assertEquals("Suzhou", result.getPlace());
        assertEquals("IMAGE", result.getResourceType());

        verify(resourceMapper).updateById(any(Resource.class));
        verify(resourceTagMapper).deleteByResourceId(resourceId);

        ArgumentCaptor<ResourceTag> tagCaptor = ArgumentCaptor.forClass(ResourceTag.class);
        verify(resourceTagMapper, times(2)).insert(tagCaptor.capture());

        java.util.List<ResourceTag> insertedTags = tagCaptor.getAllValues();
        assertEquals(11L, insertedTags.get(0).getTagId());
        assertEquals(12L, insertedTags.get(1).getTagId());
    }

    @Test
    void submitResource_shouldThrowBadRequestWhenMediaFileMissing() {
        // setup
        Long currentUserId = 1L;
        Long resourceId = 20L;

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setContributorId(currentUserId);
        resource.setStatus(ResourceStatusEnum.DRAFT.getValue());
        resource.setTitle("Title");
        resource.setDescription("Description");
        resource.setCategoryId(1L);
        resource.setResourceType("IMAGE");
        resource.setMediaUrl(null);

        when(resourceMapper.selectByIdForUpdate(resourceId)).thenReturn(resource);

        // call
        AppException exception = assertThrows(
                AppException.class,
                () -> contributorResourceService.submitResource(currentUserId, resourceId, null)
        );

        // assertion
        assertEquals(400, exception.getStatusCode());
        verify(resourceSubmissionMapper, never()).insert(any());
        verify(resourceMapper, never()).updateById(argThat(updated ->
                ResourceStatusEnum.PENDING_REVIEW.getValue().equals(updated.getStatus())
        ));
    }

    @Test
    void submitResource_shouldInsertSubmissionAndUpdateResourceStatus() {
        // setup
        Long currentUserId = 1L;
        Long resourceId = 30L;

        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setContributorId(currentUserId);
        resource.setStatus(ResourceStatusEnum.DRAFT.getValue());
        resource.setTitle("Title");
        resource.setDescription("Description");
        resource.setCategoryId(1L);
        resource.setResourceType("VIDEO");
        resource.setMediaUrl("resource-30/video.mp4");

        when(resourceMapper.selectByIdForUpdate(resourceId)).thenReturn(resource);
        ResourceSubmission latestSubmission = new ResourceSubmission();
        latestSubmission.setVersionNo(2);
        when(resourceSubmissionMapper.selectLatestByResourceId(resourceId)).thenReturn(latestSubmission);

        ResourceSubmitRequest request = new ResourceSubmitRequest();
        request.setSubmissionNote("Please review");

        // call
        contributorResourceService.submitResource(currentUserId, resourceId, request);

        // assertion
        ArgumentCaptor<ResourceSubmission> submissionCaptor = ArgumentCaptor.forClass(ResourceSubmission.class);
        verify(resourceSubmissionMapper).insert(submissionCaptor.capture());

        ResourceSubmission submission = submissionCaptor.getValue();
        assertEquals(resourceId, submission.getResourceId());
        assertEquals(currentUserId, submission.getSubmittedBy());
        assertEquals(3, submission.getVersionNo());
        assertEquals("Please review", submission.getSubmissionNote());
        assertEquals(ResourceStatusEnum.PENDING_REVIEW.getValue(), submission.getStatusSnapshot());
        assertNotNull(submission.getSubmittedAt());
        assertNotNull(submission.getCreatedAt());

        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(resourceMapper).updateById(resourceCaptor.capture());

        Resource updatedResource = resourceCaptor.getValue();
        assertEquals(ResourceStatusEnum.PENDING_REVIEW.getValue(), updatedResource.getStatus());
        assertNotNull(updatedResource.getUpdatedAt());
    }
}