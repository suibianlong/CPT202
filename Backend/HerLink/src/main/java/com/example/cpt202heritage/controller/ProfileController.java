package com.example.cpt202heritage.controller;

import com.example.cpt202heritage.entity.User;
import com.example.cpt202heritage.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/edit")
    public String showEditProfilePage(@RequestParam Long userId, Model model) {
        User user = profileService.getUserById(userId);
        model.addAttribute("user", user);
        return "profile/editProfile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("user") User formUser, Model model) {
        User updatedUser = profileService.updateProfile(formUser.getUserId(), formUser.getBio());
        model.addAttribute("user", updatedUser);
        model.addAttribute("successMessage", "Profile updated successfully.");
        return "profile/editProfile";
    }
}
