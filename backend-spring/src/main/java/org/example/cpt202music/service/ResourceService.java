package org.example.cpt202music.service;

import org.example.cpt202music.model.dto.ResourceCreateDTO;
import org.example.cpt202music.model.dto.ResourceResponseDTO;
import org.example.cpt202music.model.entity.Resource;
import java.util.List;

public interface ResourceService {

    ResourceResponseDTO createResource(Long userId, ResourceCreateDTO dto);

    List<ResourceResponseDTO> getMyResources(Long userId, String status);

    ResourceResponseDTO getResourceById(Long resourceId, Long userId);

    ResourceResponseDTO updateResource(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO saveDraft(Long resourceId, Long userId, ResourceCreateDTO dto);

    ResourceResponseDTO submitForReview(Long resourceId, Long userId);

    void deleteResource(Long resourceId, Long userId);

    List<org.example.cpt202music.model.dto.CategoryDTO> getAllCategories();

    List<org.example.cpt202music.model.dto.TagDTO> getAllTags();

    // ==================== 版本历史与回滚 ====================

    /**
     * 获取资源的版本历史记录
     */
    List<ResourceResponseDTO> getVersionHistory(Long resourceId, Long userId);

    /**
     * 回滚资源到指定版本
     * 会创建一个新版本，内容为指定版本的数据
     */
    ResourceResponseDTO rollbackToVersion(Long resourceId, Long userId, Integer targetVersionNo);
}
