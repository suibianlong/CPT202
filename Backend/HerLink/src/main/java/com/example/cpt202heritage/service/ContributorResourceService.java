package com.example.cpt202heritage.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.cpt202heritage.dto.resource.ResourceQueryRequest;
import com.example.cpt202heritage.dto.resource.ResourceSubmitRequest;
import com.example.cpt202heritage.dto.resource.ResourceUpdateRequest;
import com.example.cpt202heritage.vo.CategoryTagOptionVO;
import com.example.cpt202heritage.vo.ResourceDetailVO;
import com.example.cpt202heritage.vo.ResourceListItemVO;

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