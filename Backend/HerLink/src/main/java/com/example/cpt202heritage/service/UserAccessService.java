package com.example.cpt202heritage.service;

import java.util.List;

import com.example.cpt202heritage.dto.auth.AccountUpdateRequest;
import com.example.cpt202heritage.dto.auth.ContributorReviewDecisionRequest;
import com.example.cpt202heritage.dto.auth.LoginRequest;
import com.example.cpt202heritage.dto.auth.RegisterRequest;
import com.example.cpt202heritage.vo.ContributorRequestVO;
import com.example.cpt202heritage.vo.CurrentUserVO;

public interface UserAccessService {

    CurrentUserVO register(RegisterRequest request);

    CurrentUserVO login(LoginRequest request);

    CurrentUserVO getCurrentUserById(Long userId);

    CurrentUserVO updateAccount(Long userId, AccountUpdateRequest request);

    ContributorRequestVO submitContributorRequest(Long userId);

    ContributorRequestVO getMyLatestContributorRequest(Long userId);

    List<ContributorRequestVO> listPendingContributorRequests();

    ContributorRequestVO reviewContributorRequest(Long adminUserId, Long requestId, ContributorReviewDecisionRequest request);
}
