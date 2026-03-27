package ca.yorku.my.StudyBuddy.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ca.yorku.my.StudyBuddy.SessionLogService;
import ca.yorku.my.StudyBuddy.daos.ChatDAO;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogSummaryDTO;
import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@SpringBootTest(
    classes = {
        ca.yorku.my.StudyBuddy.controllers.StudentController.class
    }
)
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileFeaturesIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private AuthRepository authService;

    @MockBean
    private SessionLogService sessionLogService;

    @MockBean
    private ChatDAO chatDAO;

    @Test
    void getSessionLog_returnsOkWhenAuthorized() throws Exception {
        String authHeader = "Bearer token";
        when(authService.verifyFrontendToken(authHeader)).thenReturn("u1");
        when(sessionLogService.getSessionLogForStudent("u1")).thenReturn(
            new StudentSessionLogResponseDTO(new StudentSessionLogSummaryDTO(120, 2, 1, 1), java.util.List.of())
        );

        mockMvc.perform(
            get("/api/studentcontroller/profile/session-log")
                .header("Authorization", authHeader)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        verify(sessionLogService).getSessionLogForStudent("u1");
    }

    @Test
    void getSessionLog_returnsBadRequestOnIllegalArgument() throws Exception {
        String authHeader = "Bearer token";
        when(authService.verifyFrontendToken(authHeader)).thenReturn("u1");
        when(sessionLogService.getSessionLogForStudent("u1")).thenThrow(new IllegalArgumentException("bad request"));

        mockMvc.perform(
            get("/api/studentcontroller/profile/session-log")
                .header("Authorization", authHeader)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getSessionLog_returnsUnauthorizedOnSecurityException() throws Exception {
        String authHeader = "Bearer token";
        when(authService.verifyFrontendToken(authHeader)).thenThrow(new SecurityException("Unauthorized"));

        mockMvc.perform(
            get("/api/studentcontroller/profile/session-log")
                .header("Authorization", authHeader)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());

        verify(sessionLogService, never()).getSessionLogForStudent(any());
    }

    @Test
    void updateProfile_persistsLocationAndExactLocation() throws Exception {
        String authHeader = "Bearer token";
        when(authService.verifyFrontendToken(authHeader)).thenReturn("u1");

        String payload = """
            {
              "location": "Scott Library",
              "exactLocation": {
                "latitude": 43.7735,
                "longitude": -79.5019
              }
            }
            """;

        mockMvc.perform(
            post("/api/studentcontroller/profile/update")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isOk());

        verify(studentRepository).updateLocation("u1", "Scott Library");
        verify(studentRepository).updateExactLocation(eq("u1"), any(Map.class));
    }

    @Test
    void updateProfile_doesNotPersistLocationFieldsWhenMissing() throws Exception {
        String authHeader = "Bearer token";
        when(authService.verifyFrontendToken(authHeader)).thenReturn("u1");

        String payload = """
            {
              "bio": "Updated bio only"
            }
            """;

        mockMvc.perform(
            post("/api/studentcontroller/profile/update")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        ).andExpect(status().isOk());

        verify(studentRepository, never()).updateLocation(any(), any());
        verify(studentRepository, never()).updateExactLocation(any(), any());
    }
}
