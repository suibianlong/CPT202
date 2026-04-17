package com.example.cpt202heritage.service;

import com.example.cpt202heritage.dto.ResourceCreateDTO;
import com.example.cpt202heritage.dto.ResourceResponseDTO;
import com.example.cpt202heritage.entity.Resource;
import java.util.List;

public interface ResourceService {

    ResourceResponseDTO createResource(Long userId, ResourceCreateDTO dto);

    List<ResourceResponseDTO> getMyResources(Long userId, String status);

    ResourceResponseDTO getResourceById(Long resourceId, Long userId);

    ResourceResponseDTO updateResource(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO saveDraft(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO submitForReview(Long resourceId, Long userId);

    void deleteResource(Long resourceId, Long userId);

    List<com.example.cpt202heritage.dto.CategoryDTO> getAllCategories();

    List<com.example.cpt202heritage.dto.TagDTO> getAllTags();
}
