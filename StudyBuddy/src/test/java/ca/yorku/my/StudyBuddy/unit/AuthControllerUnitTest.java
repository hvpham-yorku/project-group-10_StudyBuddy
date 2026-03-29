package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.controllers.AuthController;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.yorku.my.StudyBuddy.TwoFARequiredException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthRepository authService;

    @Test
    @DisplayName("POST /api/auth/register returns 200 when registration succeeds")
    void register_success_returns200() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!",
                "firstName", "Omar",
                "lastName", "Fakousa",
                "major", "Software Engineering",
                "year", "3"
        );

        when(authService.registerUser(
                "test@yorku.ca",
                "Password123!",
                "Omar",
                "Fakousa",
                "Software Engineering",
                "3"
        )).thenReturn("uid123");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification email sent! UID: uid123"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 409 when email already exists")
    void register_emailExists_returns409() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!",
                "firstName", "Omar",
                "lastName", "Fakousa",
                "major", "Software Engineering",
                "year", "3"
        );

        when(authService.registerUser(
                "test@yorku.ca",
                "Password123!",
                "Omar",
                "Fakousa",
                "Software Engineering",
                "3"
        )).thenThrow(new RuntimeException("EMAIL_EXISTS"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Registration Failed: EMAIL_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/auth/register returns 400 on generic registration failure")
    void register_genericFailure_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!",
                "firstName", "Omar",
                "lastName", "Fakousa",
                "major", "Software Engineering",
                "year", "3"
        );

        when(authService.registerUser(
                "test@yorku.ca",
                "Password123!",
                "Omar",
                "Fakousa",
                "Software Engineering",
                "3"
        )).thenThrow(new RuntimeException("Weak password"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Registration Failed: Weak password"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password returns 400 when email is missing")
    void resetPassword_missingEmail_returns400() throws Exception {
        mvc.perform(post("/api/auth/reset-password"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Please enter your YorkU email first."));
    }



    @Test
    @DisplayName("POST /api/auth/reset-password returns 500 on unexpected error")
    void resetPassword_unexpectedError_returns500() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Server crashed"))
                .when(authService).generateResetLink("test@yorku.ca");

        mvc.perform(post("/api/auth/reset-password")
                        .param("email", "test@yorku.ca"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred."));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 when login succeeds")
    void login_success_returns200() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!"
        );

        when(authService.loginUser("test@yorku.ca", "Password123!"))
                .thenReturn("session-token-123");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("session-token-123"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 403 when login is blocked")
    void login_forbidden_returns403() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!"
        );

        when(authService.loginUser("test@yorku.ca", "Password123!"))
                .thenThrow(new IllegalStateException("Please verify your email before logging in."));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Please verify your email before logging in."));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 when user is not found")
    void login_userNotFound_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "missing@yorku.ca",
                "password", "Password123!"
        );

        when(authService.loginUser("missing@yorku.ca", "Password123!"))
                .thenThrow(new RuntimeException("User not found"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login Failed: User not found."));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 202 when 2FA is required")
    void login_twoFARequired_returns202() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "password", "Password123!"
        );

        when(authService.loginUser("test@yorku.ca", "Password123!"))
                .thenThrow(new TwoFARequiredException("2FA required"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("2FA required"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa returns 200 when OTP is valid")
    void verifyTwoFA_success_returns200() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "code", "123456"
        );

        when(authService.verifyTwoFA("test@yorku.ca", "123456"))
                .thenReturn("session-token-abc");

        mvc.perform(post("/api/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("session-token-abc"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa returns 400 when OTP is incorrect")
    void verifyTwoFA_invalidCode_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "code", "000000"
        );

        when(authService.verifyTwoFA("test@yorku.ca", "000000"))
                .thenThrow(new IllegalArgumentException("Incorrect 2FA code. Please try again."));

        mvc.perform(post("/api/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect 2FA code. Please try again."));
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa returns 500 on unexpected error")
    void verifyTwoFA_serverError_returns500() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@yorku.ca",
                "code", "123456"
        );

        when(authService.verifyTwoFA("test@yorku.ca", "123456"))
                .thenThrow(new RuntimeException("Unexpected server failure"));

        mvc.perform(post("/api/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("2FA verification failed."));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password returns 200 when email exists")
    void resetPassword_success_returns200() throws Exception {
        when(authService.generateResetLink("test@yorku.ca")).thenReturn("link-sent");

        mvc.perform(post("/api/auth/reset-password")
                        .param("email", "test@yorku.ca"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reset link generated for test@yorku.ca"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password returns 404 when email not found")
    void resetPassword_emailNotFound_returns404() throws Exception {
        when(authService.generateResetLink("unknown@yorku.ca"))
                .thenThrow(new IllegalArgumentException("No account found for that email."));

        mvc.perform(post("/api/auth/reset-password")
                        .param("email", "unknown@yorku.ca"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No account found for that email."));
    }
}