package com.cpt202.repository;

import com.cpt202.entity.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {
    List<AttachedFile> findByResourceResourceId(Long resourceId);
    long countByResourceResourceId(Long resourceId);
}
