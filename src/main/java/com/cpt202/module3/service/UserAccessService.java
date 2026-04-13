package com.cpt202.module3.service;

import com.cpt202.module3.dto.auth.AccountUpdateRequest;
import com.cpt202.module3.dto.auth.ContributorReviewDecisionRequest;
import com.cpt202.module3.dto.auth.LoginRequest;
import com.cpt202.module3.dto.auth.RegisterRequest;
import com.cpt202.module3.vo.ContributorRequestVO;
import com.cpt202.module3.vo.CurrentUserVO;
import java.util.List;

public interface UserAccessService {

    CurrentUserVO register(RegisterRequest request);

    CurrentUserVO login(LoginRequest request);

    CurrentUserVO getCurrentUserById(Long userId);

    CurrentUserVO updateAccount(Long userId, AccountUpdateRequest request);

    ContributorRequestVO submitContributorRequest(Long userId);

    ContributorRequestVO getMyLatestContributorRequest(Long userId);

    List<ContributorRequestVO> listPendingContributorRequests();

    ContributorRequestVO reviewContributorRequest(Long adminUserId,
                                                  Long requestId,
                                                  ContributorReviewDecisionRequest request);
}
