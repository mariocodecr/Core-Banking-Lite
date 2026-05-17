package com.corebanking.security;

import com.corebanking.modules.user.entity.Role;
import com.corebanking.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService unit tests")
class JwtServiceTest {

    // Valid Base64-encoded 64-byte key for HS512
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RpbmctcHVycG9zZXMtb25seS0xMjM0";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        testUser = User.create("test@corebanking.com", "hashed-password", "Test User", Role.CLIENT);
    }

    @Test
    @DisplayName("generateAccessToken — should produce non-blank token")
    void shouldGenerateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateRefreshToken — should produce non-blank token")
    void shouldGenerateRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractEmail — should return the user's email")
    void shouldExtractEmailFromToken() {
        String token = jwtService.generateAccessToken(testUser);
        String extracted = jwtService.extractEmail(token);
        assertThat(extracted).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("isTokenValid — should return true for a valid token")
    void shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — should return false for wrong user")
    void shouldReturnFalseForWrongUser() {
        String token = jwtService.generateAccessToken(testUser);
        User otherUser = User.create("other@corebanking.com", "pass", "Other", Role.ADMIN);
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid — should return false for expired token")
    void shouldReturnFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateAccessToken(testUser);
        assertThat(jwtService.isTokenValid(token, testUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid — should return false for malformed token")
    void shouldReturnFalseForMalformedToken() {
        assertThat(jwtService.isTokenValid("not.a.valid.token", testUser)).isFalse();
    }
}
