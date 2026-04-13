package com.cpt202.service;

import com.cpt202.dto.ResourceCreateDTO;
import com.cpt202.dto.ResourceResponseDTO;
import com.cpt202.entity.Resource;
import java.util.List;

public interface ResourceService {

    ResourceResponseDTO createResource(Long userId, ResourceCreateDTO dto);

    List<ResourceResponseDTO> getMyResources(Long userId, String status);

    ResourceResponseDTO getResourceById(Long resourceId, Long userId);

    ResourceResponseDTO updateResource(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO saveDraft(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO submitForReview(Long resourceId, Long userId);

    void deleteResource(Long resourceId, Long userId);

    List<com.cpt202.dto.CategoryDTO> getAllCategories();

    List<com.cpt202.dto.TagDTO> getAllTags();
}
