package com.cpt202.module3.service;

import com.cpt202.module3.dto.resource.ResourceQueryRequest;
import com.cpt202.module3.dto.resource.ResourceSubmitRequest;
import com.cpt202.module3.dto.resource.ResourceUpdateRequest;
import com.cpt202.module3.vo.CategoryTagOptionVO;
import com.cpt202.module3.vo.ResourceDetailVO;
import com.cpt202.module3.vo.ResourceListItemVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContributorResourceService {

    ResourceDetailVO createDraft(Long currentUserId);

    ResourceDetailVO updateResource(Long currentUserId, Long resourceId, ResourceUpdateRequest request);

    ResourceDetailVO uploadFiles(Long currentUserId, Long resourceId, MultipartFile previewImage, MultipartFile mediaFile);

    ResourceDetailVO getMyResourceDetail(Long currentUserId, Long resourceId);

    List<ResourceListItemVO> listMyResources(Long currentUserId, ResourceQueryRequest request);

    void submitResource(Long currentUserId, Long resourceId, ResourceSubmitRequest request);

    List<CategoryTagOptionVO> listCategoryOptions();

    List<CategoryTagOptionVO> listTagOptions();
}