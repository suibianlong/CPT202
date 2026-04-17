package com.example.cpt202heritage.controller;

import com.example.cpt202heritage.dto.auth.AccountUpdateRequest;
import com.example.cpt202heritage.dto.auth.LoginRequest;
import com.example.cpt202heritage.dto.auth.RegisterRequest;
import com.example.cpt202heritage.service.UserAccessService;
import com.example.cpt202heritage.util.ResourcePermissionChecker;
import com.example.cpt202heritage.vo.CurrentUserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccessService userAccessService;
    private final ResourcePermissionChecker resourcePermissionChecker;

    public AuthController(UserAccessService userAccessService,
                          ResourcePermissionChecker resourcePermissionChecker) {
        this.userAccessService = userAccessService;
        this.resourcePermissionChecker = resourcePermissionChecker;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentUserVO register(@RequestBody RegisterRequest request) {
        return userAccessService.register(request);
    }

    @PostMapping("/login")
    public CurrentUserVO login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        CurrentUserVO currentUser = userAccessService.login(request);
        resourcePermissionChecker.storeLoginSession(httpServletRequest, currentUser.getUserId());
        return currentUser;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        resourcePermissionChecker.clearLoginSession(request);
    }

    @GetMapping("/me")
    public CurrentUserVO getCurrentUser(HttpServletRequest request) {
        return resourcePermissionChecker.requireCurrentUser(request);
    }

    @PutMapping("/account")
    public CurrentUserVO updateAccount(@RequestBody AccountUpdateRequest request,
                                       HttpServletRequest httpServletRequest) {
        Long currentUserId = resourcePermissionChecker.requireAuthenticatedUserId(httpServletRequest);
        return userAccessService.updateAccount(currentUserId, request);
    }
}
