package org.example.cpt202music.repository;

import org.example.cpt202music.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByResourceResourceIdOrderByCreatedAtDesc(Long resourceId);
}
