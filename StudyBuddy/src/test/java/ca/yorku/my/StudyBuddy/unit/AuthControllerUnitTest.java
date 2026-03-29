package ca.yorku.my.StudyBuddy.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.yorku.my.StudyBuddy.controllers.AuthController;
import ca.yorku.my.StudyBuddy.services.AuthRepository;

@WebMvcTest(AuthController.class)
public class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthRepository authService;

    @Test
    @DisplayName("POST /api/auth/reset-password returns 200 when email exists")
    void resetPassword_Returns200_WhenEmailExists() throws Exception {
        // We don't need to mock a specific return value since the controller ignores it 
        // and just returns the generic success message.

        mockMvc.perform(post("/api/auth/reset-password")
                .param("email", "realuser@my.yorku.ca"))
                .andExpect(status().isOk())
                .andExpect(content().string("If this email is registered, a reset link has been sent."));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password returns 200 when email does NOT exist (Account Enumeration Prevention)")
    void resetPassword_Returns200_WhenEmailDoesNotExist() throws Exception {
        // Simulate the service throwing an error because the email isn't in Firebase
        doThrow(new IllegalArgumentException("No account found with this YorkU email."))
            .when(authService).generateResetLink(anyString());

        // The controller should catch this and STILL return a 200 OK with the generic message
        mockMvc.perform(post("/api/auth/reset-password")
                .param("email", "fakeuser@my.yorku.ca"))
                .andExpect(status().isOk())
                .andExpect(content().string("If this email is registered, a reset link has been sent."));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password returns 400 on missing or empty email")
    void resetPassword_Returns400_WhenEmailIsMissing() throws Exception {
        // Test with empty string
        mockMvc.perform(post("/api/auth/reset-password")
                .param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Please enter your YorkU email first."));

        // Test with completely missing parameter
        mockMvc.perform(post("/api/auth/reset-password"))
                .andExpect(status().isBadRequest());
    }
}
