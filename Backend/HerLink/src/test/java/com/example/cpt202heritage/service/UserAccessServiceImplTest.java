package com.example.cpt202heritage.service;

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
import com.example.cpt202heritage.service.impl.UserAccessServiceImpl;
import com.example.cpt202heritage.util.PasswordHashService;
import com.example.cpt202heritage.vo.ContributorRequestVO;
import com.example.cpt202heritage.vo.CurrentUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccessServiceImplTest {

    @Mock
    private AppUserMapper appUserMapper;

    @Mock
    private ContributorRequestMapper contributorRequestMapper;

    private PasswordHashService passwordHashService;
    private UserAccessServiceImpl userAccessService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashService();
        userAccessService = new UserAccessServiceImpl(appUserMapper, contributorRequestMapper, passwordHashService);
    }

    @Test
    void register_shouldCreateRegisteredViewerWithHashedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("New Viewer");
        request.setEmail("viewer@example.com");
        request.setPassword("Viewer123!");

        when(appUserMapper.selectByEmail("viewer@example.com")).thenReturn(null);
        org.mockito.Mockito.doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(10L);
            return 1;
        }).when(appUserMapper).insert(any(AppUser.class));

        CurrentUserVO result = userAccessService.register(request);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals("New Viewer", result.getName());
        assertEquals("viewer@example.com", result.getEmail());
        assertEquals(UserRoleEnum.REGISTERED_VIEWER.getValue(), result.getRole());
        assertEquals("NONE", result.getContributorStatus());
        assertFalse(result.isContributor());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserMapper).insert(captor.capture());
        AppUser inserted = captor.getValue();
        assertEquals(UserRoleEnum.REGISTERED_VIEWER.getValue(), inserted.getRole());
        assertTrue(passwordHashService.matches("Viewer123!", inserted.getPasswordHash()));
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Existing");
        request.setEmail("viewer@example.com");
        request.setPassword("Viewer123!");

        AppUser existing = new AppUser();
        existing.setUserId(1L);
        when(appUserMapper.selectByEmail("viewer@example.com")).thenReturn(existing);

        AppException exception = assertThrows(AppException.class, () -> userAccessService.register(request));

        assertEquals(409, exception.getStatusCode());
        verify(appUserMapper, never()).insert(any(AppUser.class));
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("viewer@example.com");
        request.setPassword("WrongPass123!");

        AppUser user = new AppUser();
        user.setUserId(3L);
        user.setEmail("viewer@example.com");
        user.setPasswordHash(passwordHashService.hash("Viewer123!"));
        when(appUserMapper.selectByEmail("viewer@example.com")).thenReturn(user);

        AppException exception = assertThrows(AppException.class, () -> userAccessService.login(request));

        assertEquals(401, exception.getStatusCode());
    }

    @Test
    void updateAccount_shouldRejectDuplicateEmail() {
        AppUser currentUser = new AppUser();
        currentUser.setUserId(3L);
        currentUser.setName("Viewer");
        currentUser.setEmail("viewer@example.com");
        currentUser.setRole(UserRoleEnum.REGISTERED_VIEWER.getValue());

        AppUser otherUser = new AppUser();
        otherUser.setUserId(4L);
        otherUser.setEmail("other@example.com");

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("Viewer Updated");
        request.setEmail("other@example.com");

        when(appUserMapper.selectById(3L)).thenReturn(currentUser);
        when(appUserMapper.selectByEmail("other@example.com")).thenReturn(otherUser);

        AppException exception = assertThrows(AppException.class, () -> userAccessService.updateAccount(3L, request));

        assertEquals(409, exception.getStatusCode());
        verify(appUserMapper, never()).updateBasicInfo(any(AppUser.class));
    }

    @Test
    void submitContributorRequest_shouldRejectWhenPendingAlreadyExists() {
        AppUser currentUser = new AppUser();
        currentUser.setUserId(3L);
        currentUser.setRole(UserRoleEnum.REGISTERED_VIEWER.getValue());
        currentUser.setName("Viewer");
        currentUser.setEmail("viewer@example.com");

        ContributorRequest latestRequest = new ContributorRequest();
        latestRequest.setRequestId(11L);
        latestRequest.setStatus(ContributorApplicationStatusEnum.PENDING.getValue());

        when(appUserMapper.selectById(3L)).thenReturn(currentUser);
        when(contributorRequestMapper.selectLatestByUserId(3L)).thenReturn(latestRequest);

        AppException exception = assertThrows(AppException.class, () -> userAccessService.submitContributorRequest(3L));

        assertEquals(409, exception.getStatusCode());
        verify(contributorRequestMapper, never()).insert(any(ContributorRequest.class));
    }

    @Test
    void reviewContributorRequest_shouldApprovePendingRequest() {
        ContributorReviewDecisionRequest request = new ContributorReviewDecisionRequest();
        request.setDecision("APPROVED");
        request.setReviewComment("Approved for contributor access.");

        ContributorRequest pendingRequest = new ContributorRequest();
        pendingRequest.setRequestId(20L);
        pendingRequest.setUserId(4L);
        pendingRequest.setStatus(ContributorApplicationStatusEnum.PENDING.getValue());
        pendingRequest.setRequestedAt(LocalDateTime.now().minusDays(1));
        pendingRequest.setCreatedAt(LocalDateTime.now().minusDays(1));
        pendingRequest.setUpdatedAt(LocalDateTime.now().minusDays(1));

        ContributorRequestVO requestView = new ContributorRequestVO();
        requestView.setRequestId(20L);
        requestView.setUserId(4L);
        requestView.setUserName("Pending Applicant");
        requestView.setUserEmail("pending@example.com");
        requestView.setStatus(ContributorApplicationStatusEnum.APPROVED.getValue());

        when(contributorRequestMapper.selectByIdForUpdate(20L)).thenReturn(pendingRequest);
        when(contributorRequestMapper.selectRequestViewById(20L)).thenReturn(requestView);

        ContributorRequestVO result = userAccessService.reviewContributorRequest(1L, 20L, request);

        assertEquals(ContributorApplicationStatusEnum.APPROVED.getValue(), result.getStatus());
        ArgumentCaptor<ContributorRequest> captor = ArgumentCaptor.forClass(ContributorRequest.class);
        verify(contributorRequestMapper).updateReviewDecision(captor.capture());
        assertEquals(ContributorApplicationStatusEnum.APPROVED.getValue(), captor.getValue().getStatus());
        assertEquals(1L, captor.getValue().getReviewedBy());
        assertEquals("Approved for contributor access.", captor.getValue().getReviewComment());
    }
}
