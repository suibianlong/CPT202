package com.cpt202.group8.heritage.repositories;

import com.cpt202.group8.heritage.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByResourceIdOrderByCreatedAtDesc(Long resourceId);

    boolean existsByResourceIdAndUserIdAndContentAndCreatedAtAfter(
            Long resourceId,
            Long userId,
            String content,
            LocalDateTime time
    );
}