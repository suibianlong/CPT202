package com.cpt202.group8.heritage.repositories;

import com.cpt202.group8.heritage.entities.Resource;
import com.cpt202.group8.heritage.entities.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByStatus(ResourceStatus status);

    List<Resource> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            ResourceStatus status1, String titleKeyword,
            ResourceStatus status2, String descriptionKeyword
    );

    List<Resource> findByStatusAndCategoryId(ResourceStatus status, Long categoryId);

    List<Resource> findByStatusOrderByTitleAsc(ResourceStatus status);

    List<Resource> findByStatusOrderByReviewedAtDesc(ResourceStatus status);

    List<Resource> findByStatusAndCategoryIdOrderByTitleAsc(ResourceStatus status, Long categoryId);

    List<Resource> findByStatusAndCategoryIdOrderByReviewedAtDesc(ResourceStatus status, Long categoryId);
}