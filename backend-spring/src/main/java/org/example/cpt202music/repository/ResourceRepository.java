package org.example.cpt202music.repository;

import org.example.cpt202music.model.entity.Resource;
import org.example.cpt202music.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByContributor(User contributor);

    List<Resource> findByContributorOrderByLastUpdatedTimeDesc(User contributor);

    List<Resource> findByContributorAndStatus(User contributor, Resource.ResourceStatus status);

    @Query("SELECT r FROM Resource r WHERE r.contributor.userId = :userId ORDER BY r.lastUpdatedTime DESC")
    List<Resource> findByContributorIdOrderByLastUpdatedTimeDesc(@Param("userId") Long userId);

    @Query("SELECT r FROM Resource r WHERE r.contributor.userId = :userId AND r.status = :status ORDER BY r.lastUpdatedTime DESC")
    List<Resource> findByContributorIdAndStatusOrderByLastUpdatedTimeDesc(
            @Param("userId") Long userId,
            @Param("status") Resource.ResourceStatus status);

    @Query("SELECT r FROM Resource r LEFT JOIN FETCH r.categories LEFT JOIN FETCH r.tags WHERE r.resourceId = :id")
    Optional<Resource> findByIdWithCategoriesAndTags(@Param("id") Long id);
}
