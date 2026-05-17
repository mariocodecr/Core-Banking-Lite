package com.corebanking.modules.auth.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.modules.auth.dto.LoginRequest;
import com.corebanking.modules.auth.dto.LoginResponse;
import com.corebanking.modules.auth.dto.RefreshTokenRequest;
import com.corebanking.modules.user.entity.User;
import com.corebanking.modules.user.repository.UserRepository;
import com.corebanking.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (LockedException e) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        log.info("Successful login: email={}, role={}", user.getEmail(), user.getRole());

        return buildLoginResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        String email = jwtService.extractEmail(request.getRefreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        if (!jwtService.isTokenValid(request.getRefreshToken(), user)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        log.info("Token refreshed for: {}", email);

        return buildLoginResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user
        );
    }

    @Override
    public void logout(String token) {
        // Phase 10: invalidate token via Redis blacklist
        String email = jwtService.extractEmail(token);
        log.info("User logged out: {}", email);
    }

    private LoginResponse buildLoginResponse(String accessToken, String refreshToken, User user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpiration())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
