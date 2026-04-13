package com.cpt202.task.controller;

import com.cpt202.task.entity.ContributorApplication;
import com.cpt202.task.service.ApprovalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/contributor-applications")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping
    public String showPendingApplications(Model model) {
        model.addAttribute("pendingApplications", approvalService.getPendingApplications());
        return "admin/pendingApplications";
    }

    @GetMapping("/{applicationId}")
    public String showApplicationDetail(@PathVariable Long applicationId, Model model) {
        ContributorApplication application = approvalService.getApplicationById(applicationId);
        model.addAttribute("application", application);
        return "admin/applicationDetail";
    }

    @PostMapping("/{applicationId}/approve")
    public String approveApplication(@PathVariable Long applicationId,
                                     @RequestParam Long reviewerId,
                                     @RequestParam(required = false) String reviewComment) {
        approvalService.approveApplication(applicationId, reviewerId, reviewComment);
        return "redirect:/admin/contributor-applications";
    }

    @PostMapping("/{applicationId}/reject")
    public String rejectApplication(@PathVariable Long applicationId,
                                    @RequestParam Long reviewerId,
                                    @RequestParam(required = false) String reviewComment) {
        approvalService.rejectApplication(applicationId, reviewerId, reviewComment);
        return "redirect:/admin/contributor-applications";
    }
}
