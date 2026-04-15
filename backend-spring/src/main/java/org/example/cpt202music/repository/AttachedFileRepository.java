package org.example.cpt202music.repository;

import org.example.cpt202music.model.entity.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Long> {
    List<AttachedFile> findByResourceResourceId(Long resourceId);
    long countByResourceResourceId(Long resourceId);
}
