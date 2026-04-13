package com.cpt202.task.controller;

import com.cpt202.task.entity.ContributorApplication;
import com.cpt202.task.entity.User;
import com.cpt202.task.service.ContributorApplicationService;
import com.cpt202.task.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/contributor")
public class ContributorApplicationController {

    private final ContributorApplicationService contributorApplicationService;
    private final ProfileService profileService;

    public ContributorApplicationController(ContributorApplicationService contributorApplicationService,
                                            ProfileService profileService) {
        this.contributorApplicationService = contributorApplicationService;
        this.profileService = profileService;
    }

    @GetMapping("/apply")
    public String showApplyPage(@RequestParam Long userId, Model model) {
        User user = profileService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("applicationForm", new ContributorApplication());
        return "contributor/applyContributor";
    }

    @PostMapping("/apply")
    public String submitApplication(@RequestParam Long userId,
                                    @ModelAttribute("applicationForm") ContributorApplication applicationForm,
                                    Model model) {
        try {
            contributorApplicationService.createApplication(userId, applicationForm.getApplicationReason());
            return "redirect:/contributor/status?userId=" + userId;
        } catch (IllegalStateException ex) {
            User user = profileService.getUserById(userId);
            model.addAttribute("user", user);
            model.addAttribute("applicationForm", applicationForm);
            model.addAttribute("errorMessage", ex.getMessage());
            return "contributor/applyContributor";
        }
    }

    @GetMapping("/status")
    public String showApplicationStatus(@RequestParam Long userId, Model model) {
        User user = profileService.getUserById(userId);
        ContributorApplication latestApplication = contributorApplicationService.getLatestApplication(userId);

        model.addAttribute("user", user);
        model.addAttribute("latestApplication", latestApplication);
        return "contributor/applicationStatus";
    }
}
