package com.cpt202.repository;

import com.cpt202.entity.ResourceSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ResourceSubmissionRepository extends JpaRepository<ResourceSubmission, Long> {

    @Query("SELECT COALESCE(MAX(rs.versionNo), 0) FROM ResourceSubmission rs WHERE rs.resource.resourceId = :resourceId")
    Integer findMaxVersionNoByResourceId(@Param("resourceId") Long resourceId);

    @Query("SELECT rs FROM ResourceSubmission rs WHERE rs.resource.resourceId = :resourceId ORDER BY rs.versionNo DESC")
    Optional<ResourceSubmission> findLatestByResourceId(@Param("resourceId") Long resourceId);
}
