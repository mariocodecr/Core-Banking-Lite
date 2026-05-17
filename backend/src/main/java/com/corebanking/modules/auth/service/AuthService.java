package com.corebanking.modules.auth.service;

import com.corebanking.modules.auth.dto.LoginRequest;
import com.corebanking.modules.auth.dto.LoginResponse;
import com.corebanking.modules.auth.dto.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(String token);
}
