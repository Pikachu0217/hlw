package com.hlw.auth.domain.dto;

import com.hlw.auth.domain.resp.LoginUserResp;

public interface TokenIssuer {
    String issue(LoginUserResp user);
}
