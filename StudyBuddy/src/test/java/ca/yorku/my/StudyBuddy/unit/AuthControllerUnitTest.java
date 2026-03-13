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
}