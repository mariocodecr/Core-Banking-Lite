package com.corebanking.integration;

import com.corebanking.modules.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController integration tests")
class AuthControllerIT extends BaseIntegrationTest {

    private static final String EMAIL    = "admin.it@test.com";
    private static final String PASSWORD = "admin123";

    @BeforeEach
    void seedUsers() {
        seedUser(EMAIL, PASSWORD, Role.ADMIN);
    }

    @Test
    @DisplayName("POST /auth/login — valid credentials return access and refresh tokens")
    void login_validCredentials_returnsTokens() throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken",  not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login — wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        String body = """
                {"email": "%s", "password": "wrong-password"}
                """.formatted(EMAIL);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("CBL-012"));
    }

    @Test
    @DisplayName("POST /auth/login — unknown email returns 401")
    void login_unknownEmail_returns401() throws Exception {
        String body = """
                {"email": "noexiste@test.com", "password": "whatever"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/refresh — valid refresh token returns new access token")
    void refresh_validToken_returnsNewAccessToken() throws Exception {
        // First login to get refresh token
        String loginBody = """
                {"email": "%s", "password": "%s"}
                """.formatted(EMAIL, PASSWORD);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).path("refreshToken").asText();

        String refreshBody = """
                {"refreshToken": "%s"}
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())));
    }

    @Test
    @DisplayName("POST /auth/login — missing email field returns 400")
    void login_missingEmail_returns400() throws Exception {
        String body = """
                {"password": "somepassword"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
