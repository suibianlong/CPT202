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

            // 临时测试用！！！：手动设置当前用户
        session.setAttribute("userId", 4L);
        session.setAttribute("userRole", "reviewer");


        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (resource.getStatus() != ResourceStatus.Approved) {
            throw new IllegalArgumentException("This resource item is not accessible.");
        }

        List<Comment> comments = commentService.getCommentsByResourceId(id);

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = 1L;//后面改成跳转登录界面
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null) {
            userRole = "user";
        }

        model.addAttribute("resource", resource);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserRole", userRole);

        return "detail";
    }

    @PostMapping("/{id}/comments")
    public String postComment(@PathVariable Long id,
                              @RequestParam("content") String content,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            userId = 1L; //后面对接了改成跳转登录界面
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
            userId = 1L;//后面对接了改成跳转登录界面
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null) {
            userRole = "user";
        }

        try {
            commentService.deleteComment(commentId, userId, userRole);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comment deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("commentError", e.getMessage());
        }

        return "redirect:/page/resources/" + resourceId;
    }
}