package com.cpt202.repository;

import com.cpt202.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByResourceResourceIdOrderByCreatedAtDesc(Long resourceId);
}
