package com.example.cpt202heritage.service.impl;

import com.example.cpt202heritage.dto.auth.AccountUpdateRequest;
import com.example.cpt202heritage.dto.auth.ContributorReviewDecisionRequest;
import com.example.cpt202heritage.dto.auth.LoginRequest;
import com.example.cpt202heritage.dto.auth.RegisterRequest;
import com.example.cpt202heritage.entity.AppUser;
import com.example.cpt202heritage.entity.ContributorRequest;
import com.example.cpt202heritage.enums.ContributorApplicationStatusEnum;
import com.example.cpt202heritage.enums.UserRoleEnum;
import com.example.cpt202heritage.exception.AppException;
import com.example.cpt202heritage.mapper.AppUserMapper;
import com.example.cpt202heritage.mapper.ContributorRequestMapper;
import com.example.cpt202heritage.service.UserAccessService;
import com.example.cpt202heritage.util.PasswordHashService;
import com.example.cpt202heritage.vo.ContributorRequestVO;
import com.example.cpt202heritage.vo.CurrentUserVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccessServiceImpl implements UserAccessService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserMapper appUserMapper;
    private final ContributorRequestMapper contributorRequestMapper;
    private final PasswordHashService passwordHashService;

    public UserAccessServiceImpl(AppUserMapper appUserMapper,
                                 ContributorRequestMapper contributorRequestMapper,
                                 PasswordHashService passwordHashService) {
        this.appUserMapper = appUserMapper;
        this.contributorRequestMapper = contributorRequestMapper;
        this.passwordHashService = passwordHashService;
    }

    @Override
    @Transactional
    public CurrentUserVO register(RegisterRequest request) {
        String name = trimToNull(request == null ? null : request.getName());
        String email = normalizeEmail(request == null ? null : request.getEmail());
        String password = request == null ? null : request.getPassword();

        validateRegistrationInput(name, email, password);

        if (appUserMapper.selectByEmail(email) != null) {
            throw AppException.conflict("The email address is already in use.");
        }

        LocalDateTime now = LocalDateTime.now();
        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHashService.hash(password));
        user.setRole(UserRoleEnum.REGISTERED_VIEWER.getValue());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        appUserMapper.insert(user);

        return buildCurrentUserVO(user, null);
    }

    @Override
    public CurrentUserVO login(LoginRequest request) {
        String email = normalizeEmail(request == null ? null : request.getEmail());
        String password = request == null ? null : request.getPassword();
        validateLoginInput(email, password);

        AppUser user = appUserMapper.selectByEmail(email);
        if (user == null || !passwordHashService.matches(password, user.getPasswordHash())) {
            throw AppException.unauthorized("Invalid email or password.");
        }

        ContributorRequest latestRequest = contributorRequestMapper.selectLatestByUserId(user.getUserId());
        return buildCurrentUserVO(user, latestRequest);
    }

    @Override
    public CurrentUserVO getCurrentUserById(Long userId) {
        AppUser user = loadUser(userId);
        ContributorRequest latestRequest = contributorRequestMapper.selectLatestByUserId(userId);
        return buildCurrentUserVO(user, latestRequest);
    }

    @Override
    @Transactional
    public CurrentUserVO updateAccount(Long userId, AccountUpdateRequest request) {
        String name = trimToNull(request == null ? null : request.getName());
        String email = normalizeEmail(request == null ? null : request.getEmail());
        validateAccountUpdateInput(name, email);

        AppUser existingUser = loadUser(userId);
        AppUser userByEmail = appUserMapper.selectByEmail(email);
        if (userByEmail != null && !Objects.equals(userByEmail.getUserId(), userId)) {
            throw AppException.conflict("The email address is already in use.");
        }

        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setUpdatedAt(LocalDateTime.now());
        appUserMapper.updateBasicInfo(existingUser);

        ContributorRequest latestRequest = contributorRequestMapper.selectLatestByUserId(userId);
        return buildCurrentUserVO(existingUser, latestRequest);
    }

    @Override
    @Transactional
    public ContributorRequestVO submitContributorRequest(Long userId) {
        AppUser user = loadUser(userId);
        if (!UserRoleEnum.REGISTERED_VIEWER.getValue().equalsIgnoreCase(user.getRole())) {
            throw AppException.forbidden("Only registered viewers can submit contributor requests.");
        }

        ContributorRequest latestRequest = contributorRequestMapper.selectLatestByUserId(userId);
        if (latestRequest != null) {
            String latestStatus = latestRequest.getStatus();
            if (ContributorApplicationStatusEnum.PENDING.getValue().equalsIgnoreCase(latestStatus)) {
                throw AppException.conflict("Your existing contributor request is still under review.");
            }
            if (ContributorApplicationStatusEnum.APPROVED.getValue().equalsIgnoreCase(latestStatus)) {
                throw AppException.conflict("You are already an approved contributor.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        ContributorRequest contributorRequest = new ContributorRequest();
        contributorRequest.setUserId(userId);
        contributorRequest.setStatus(ContributorApplicationStatusEnum.PENDING.getValue());
        contributorRequest.setRequestedAt(now);
        contributorRequest.setCreatedAt(now);
        contributorRequest.setUpdatedAt(now);
        contributorRequestMapper.insert(contributorRequest);

        return buildContributorRequestVO(contributorRequest, user);
    }

    @Override
    public ContributorRequestVO getMyLatestContributorRequest(Long userId) {
        AppUser user = loadUser(userId);
        ContributorRequest latestRequest = contributorRequestMapper.selectLatestByUserId(userId);
        if (latestRequest == null) {
            return null;
        }
        return buildContributorRequestVO(latestRequest, user);
    }

    @Override
    public List<ContributorRequestVO> listPendingContributorRequests() {
        return contributorRequestMapper.selectPendingRequestViews();
    }

    @Override
    @Transactional
    public ContributorRequestVO reviewContributorRequest(Long adminUserId,
                                                         Long requestId,
                                                         ContributorReviewDecisionRequest request) {
        String decision = normalizeDecision(request == null ? null : request.getDecision());
        String reviewComment = trimToNull(request == null ? null : request.getReviewComment());

        if (!ContributorApplicationStatusEnum.APPROVED.getValue().equals(decision)
                && !ContributorApplicationStatusEnum.REJECTED.getValue().equals(decision)) {
            throw AppException.badRequest("Decision must be APPROVED or REJECTED.");
        }

        ContributorRequest contributorRequest = contributorRequestMapper.selectByIdForUpdate(requestId);
        if (contributorRequest == null) {
            throw AppException.notFound("Contributor request does not exist.");
        }

        if (!ContributorApplicationStatusEnum.PENDING.getValue().equalsIgnoreCase(contributorRequest.getStatus())) {
            throw AppException.conflict("Only pending contributor requests can be reviewed.");
        }

        LocalDateTime now = LocalDateTime.now();
        contributorRequest.setStatus(decision);
        contributorRequest.setReviewedAt(now);
        contributorRequest.setReviewedBy(adminUserId);
        contributorRequest.setReviewComment(reviewComment);
        contributorRequest.setUpdatedAt(now);
        contributorRequestMapper.updateReviewDecision(contributorRequest);

        ContributorRequestVO requestView = contributorRequestMapper.selectRequestViewById(requestId);
        if (requestView != null) {
            return requestView;
        }

        AppUser applicant = loadUser(contributorRequest.getUserId());
        return buildContributorRequestVO(contributorRequest, applicant);
    }

    private AppUser loadUser(Long userId) {
        if (userId == null) {
            throw AppException.unauthorized("Please log in first.");
        }

        AppUser user = appUserMapper.selectById(userId);
        if (user == null) {
            throw AppException.notFound("Current user does not exist.");
        }
        return user;
    }

    private void validateRegistrationInput(String name, String email, String password) {
        List<String> details = new ArrayList<>();

        if (name == null) {
            details.add("Name is required.");
        }
        if (email == null) {
            details.add("Email is required.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            details.add("Please enter a valid email address.");
        }
        if (password == null || password.isBlank()) {
            details.add("Password is required.");
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            details.add("Password must be at least 8 characters long.");
        }

        if (!details.isEmpty()) {
            throw AppException.badRequest("Please correct the registration form.", details);
        }
    }

    private void validateLoginInput(String email, String password) {
        List<String> details = new ArrayList<>();

        if (email == null) {
            details.add("Email is required.");
        }
        if (password == null || password.isBlank()) {
            details.add("Password is required.");
        }

        if (!details.isEmpty()) {
            throw AppException.badRequest("Please complete the login form.", details);
        }
    }

    private void validateAccountUpdateInput(String name, String email) {
        List<String> details = new ArrayList<>();

        if (name == null) {
            details.add("Name is required.");
        }
        if (email == null) {
            details.add("Email is required.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            details.add("Please enter a valid email address.");
        }

        if (!details.isEmpty()) {
            throw AppException.badRequest("Please correct your account settings.", details);
        }
    }

    private String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        if (trimmed == null) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizeDecision(String decision) {
        String trimmed = trimToNull(decision);
        if (trimmed == null) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CurrentUserVO buildCurrentUserVO(AppUser user, ContributorRequest latestRequest) {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setUserId(user.getUserId());
        currentUserVO.setName(user.getName());
        currentUserVO.setEmail(user.getEmail());
        currentUserVO.setRole(user.getRole());
        currentUserVO.setLatestContributorRequestId(latestRequest == null ? null : latestRequest.getRequestId());

        String contributorStatus = latestRequest == null ? "NONE" : latestRequest.getStatus();
        currentUserVO.setContributorStatus(contributorStatus);
        currentUserVO.setContributor(
                ContributorApplicationStatusEnum.APPROVED.getValue().equalsIgnoreCase(contributorStatus)
        );

        return currentUserVO;
    }

    private ContributorRequestVO buildContributorRequestVO(ContributorRequest contributorRequest, AppUser user) {
        ContributorRequestVO contributorRequestVO = new ContributorRequestVO();
        contributorRequestVO.setRequestId(contributorRequest.getRequestId());
        contributorRequestVO.setUserId(contributorRequest.getUserId());
        contributorRequestVO.setReviewedBy(contributorRequest.getReviewedBy());
        contributorRequestVO.setUserName(user.getName());
        contributorRequestVO.setUserEmail(user.getEmail());
        contributorRequestVO.setStatus(contributorRequest.getStatus());
        contributorRequestVO.setReviewComment(contributorRequest.getReviewComment());
        contributorRequestVO.setRequestedAt(contributorRequest.getRequestedAt());
        contributorRequestVO.setReviewedAt(contributorRequest.getReviewedAt());
        contributorRequestVO.setCreatedAt(contributorRequest.getCreatedAt());
        contributorRequestVO.setUpdatedAt(contributorRequest.getUpdatedAt());
        return contributorRequestVO;
    }
}
