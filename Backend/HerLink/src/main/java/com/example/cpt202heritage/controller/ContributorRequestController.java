package com.example.cpt202heritage.controller;

import com.example.cpt202heritage.service.UserAccessService;
import com.example.cpt202heritage.util.ResourcePermissionChecker;
import com.example.cpt202heritage.vo.ContributorRequestVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contributor-requests")
public class ContributorRequestController {

    private final UserAccessService userAccessService;
    private final ResourcePermissionChecker resourcePermissionChecker;

    public ContributorRequestController(UserAccessService userAccessService,
                                        ResourcePermissionChecker resourcePermissionChecker) {
        this.userAccessService = userAccessService;
        this.resourcePermissionChecker = resourcePermissionChecker;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContributorRequestVO submitContributorRequest(HttpServletRequest request) {
        Long currentUserId = resourcePermissionChecker.requireAuthenticatedUserId(request);
        return userAccessService.submitContributorRequest(currentUserId);
    }

    @GetMapping("/me")
    public ContributorRequestVO getMyLatestContributorRequest(HttpServletRequest request) {
        Long currentUserId = resourcePermissionChecker.requireAuthenticatedUserId(request);
        return userAccessService.getMyLatestContributorRequest(currentUserId);
    }
}
