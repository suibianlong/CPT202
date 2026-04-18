package com.cpt202.group8.heritage.controllers;

import com.cpt202.group8.heritage.services.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/page/feedback")
public class FeedbackPageController {

    private final FeedbackService feedbackService;

    public FeedbackPageController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public String showFeedbackPage(Model model, HttpSession session) {
        session.setAttribute("userId", 3L);
        session.setAttribute("userRole", "reviewer");

        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");

        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserRole", userRole);
        model.addAttribute("feedbackList", feedbackService.getVisibleFeedbacks(userId, userRole));

        return "feedback";
    }

    @PostMapping
    public String submitFeedback(@RequestParam("feedbackType") String feedbackType,
                                 @RequestParam("description") String description,
                                 @RequestParam(value = "files", required = false) MultipartFile[] files,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = 2L;
            session.setAttribute("userId", userId);
        }

        try {
            feedbackService.submitFeedback(userId, feedbackType, description, files);
            redirectAttributes.addFlashAttribute("feedbackSuccess", "Feedback submitted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("feedbackError", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("feedbackError", "File upload failed.");
        }

        return "redirect:/page/feedback";
    }
}