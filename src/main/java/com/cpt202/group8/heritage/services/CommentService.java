package com.cpt202.group8.heritage.services;

import com.cpt202.group8.heritage.entities.Comment;
import com.cpt202.group8.heritage.entities.Resource;
import com.cpt202.group8.heritage.entities.ResourceStatus;
import com.cpt202.group8.heritage.repositories.CommentRepository;
import com.cpt202.group8.heritage.repositories.ResourceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ResourceRepository resourceRepository;

    public CommentService(CommentRepository commentRepository, ResourceRepository resourceRepository) {
        this.commentRepository = commentRepository;
        this.resourceRepository = resourceRepository;
    }

    public List<Comment> getCommentsByResourceId(Long resourceId) {
        return commentRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
    }

    public void addComment(Long resourceId, Long userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("You haven’t entered a comment yet.");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (resource.getStatus() != ResourceStatus.APPROVED) {
            throw new IllegalArgumentException("This resource is not available for commenting.");
        }
        
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);

        boolean duplicated = commentRepository.existsByResourceIdAndUserIdAndContentAndCreatedAtAfter(
                resourceId,
                userId,
                content.trim(),
                thirtySecondsAgo
        );

        if (duplicated) {
            throw new IllegalArgumentException("Please do not repeatedly post your comments.");
        }

        Comment comment = new Comment();
        comment.setResourceId(resourceId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found."));

    if (!comment.getUserId().equals(userId)) {
        throw new IllegalArgumentException("You can only delete your own comment.");
    }

    commentRepository.delete(comment);
}
}