package com.example.cpt202heritage.util;

import com.example.cpt202heritage.enums.UserRoleEnum;
import com.example.cpt202heritage.exception.AppException;
import com.example.cpt202heritage.service.UserAccessService;
import com.example.cpt202heritage.vo.CurrentUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class ResourcePermissionChecker {

    private final UserAccessService userAccessService;

    public ResourcePermissionChecker(UserAccessService userAccessService) {
        this.userAccessService = userAccessService;
    }

    public CurrentUserVO requireCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw AppException.unauthorized("Please log in first.");
        }

        Object userIdValue = session.getAttribute(SessionKeys.USER_ID);
        if (userIdValue == null) {
            throw AppException.unauthorized("Please log in first.");
        }

        Long currentUserId = parseLongValue(userIdValue);
        try {
            return userAccessService.getCurrentUserById(currentUserId);
        } catch (AppException exception) {
            if (exception.getStatusCode() == 404) {
                clearLoginSession(request);
                throw AppException.unauthorized("Your session is no longer valid. Please log in again.");
            }
            throw exception;
        }
    }

    public Long requireAuthenticatedUserId(HttpServletRequest request) {
        return requireCurrentUser(request).getUserId();
    }

    public Long requireContributorUserId(HttpServletRequest request) {
        CurrentUserVO currentUser = requireCurrentUser(request);
        if (!currentUser.isContributor()) {
            throw AppException.forbidden("Contributor access requires an approved contributor request.");
        }
        return currentUser.getUserId();
    }

    public Long requireAdminUserId(HttpServletRequest request) {
        CurrentUserVO currentUser = requireCurrentUser(request);
        if (!UserRoleEnum.ADMINISTRATOR.getValue().equalsIgnoreCase(currentUser.getRole())) {
            throw AppException.forbidden("Administrator permission is required.");
        }
        return currentUser.getUserId();
    }

    public void storeLoginSession(HttpServletRequest request, Long userId) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionKeys.USER_ID, userId);
    }

    public void clearLoginSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private Long parseLongValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        String text = value == null ? "" : value.toString().trim();
        if (text.isEmpty()) {
            throw AppException.unauthorized("Current user session is incomplete.");
        }

        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw AppException.unauthorized("Current user identity is invalid.");
        }
    }
}
