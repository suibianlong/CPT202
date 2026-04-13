package com.cpt202.repository;

import com.cpt202.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);
    List<Tag> findByStatus(Tag.TagStatus status);
    List<Tag> findByTagNameIn(List<String> tagNames);
}
