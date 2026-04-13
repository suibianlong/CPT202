package com.cpt202.group8.heritage.controllers;

import com.cpt202.group8.heritage.entities.Comment;
import com.cpt202.group8.heritage.entities.Resource;
import com.cpt202.group8.heritage.entities.ResourceStatus;
import com.cpt202.group8.heritage.repositories.ResourceRepository;
import com.cpt202.group8.heritage.services.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/page/resources")
public class ResourcePageController {

    private final ResourceRepository resourceRepository;
    private final CommentService commentService;

    public ResourcePageController(ResourceRepository resourceRepository, CommentService commentService) {
        this.resourceRepository = resourceRepository;
        this.commentService = commentService;
    }

    @GetMapping("/{id}")
    public String showDetailPage(@PathVariable Long id, Model model, HttpSession session) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (resource.getStatus() != ResourceStatus.APPROVED) {
            throw new IllegalArgumentException("This resource item is not accessible.");
        }

        List<Comment> comments = commentService.getCommentsByResourceId(id);

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = 1L;
        }

        model.addAttribute("resource", resource);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserId", userId);

        return "detail";
    }

    @PostMapping("/{id}/comments")
    public String postComment(@PathVariable Long id,
                              @RequestParam("content") String content,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            userId = 1L;
        }

        try {
            commentService.addComment(id, userId, content);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comment posted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }

        return "redirect:/page/resources/" + id;
    }

    @PostMapping("/{resourceId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long resourceId,
                                @PathVariable Long commentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            userId = 1L;
        }

        try {
            commentService.deleteComment(commentId, userId);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comment deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }

        return "redirect:/page/resources/" + resourceId;
    }
}