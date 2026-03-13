package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.controllers.ConnectionsController;
import ca.yorku.my.StudyBuddy.services.ConnectionsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ConnectionsController.class)
class ConnectionsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ConnectionsService connectionsService;

    private ConnectionsService.ConnectionDTO buildDto(
            String userId,
            String fullName,
            String program,
            String profilePic,
            String[] courses,
            Long lastActiveAt,
            String activityStatus
    ) {
        ConnectionsService.ConnectionDTO dto = new ConnectionsService.ConnectionDTO();
        dto.userId = userId;
        dto.fullName = fullName;
        dto.program = program;
        dto.profilePic = profilePic;
        dto.courses = courses;
        dto.lastActiveAt = lastActiveAt;
        dto.activityStatus = activityStatus;
        return dto;
    }

    @Test
    @DisplayName("GET /api/connections without userId returns 400")
    void getConnections_missingUserId_returns400() throws Exception {
        mvc.perform(get("/api/connections"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(connectionsService);
    }

    @Test
    @DisplayName("GET /api/connections returns 200 and empty list")
    void getConnections_returnsEmptyList() throws Exception {
        when(connectionsService.getAcceptedConnections("uid_me")).thenReturn(List.of());

        mvc.perform(get("/api/connections").param("userId", "uid_me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(connectionsService).getAcceptedConnections("uid_me");
    }

    @Test
    @DisplayName("GET /api/connections returns DTO list with correct JSON structure")
    void getConnections_returnsConnections() throws Exception {
        ConnectionsService.ConnectionDTO dto = buildDto(
                "uid_other",
                "Yash",
                "Computer Engineering",
                "",
                new String[]{"EECS 2101"},
                1712000000000L,
                "online"
        );

        when(connectionsService.getAcceptedConnections("uid_me")).thenReturn(List.of(dto));

        mvc.perform(get("/api/connections").param("userId", "uid_me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value("uid_other"))
                .andExpect(jsonPath("$[0].fullName").value("Yash"))
                .andExpect(jsonPath("$[0].program").value("Computer Engineering"))
                .andExpect(jsonPath("$[0].profilePic").value(""))
                .andExpect(jsonPath("$[0].courses[0]").value("EECS 2101"))
                .andExpect(jsonPath("$[0].lastActiveAt").value(1712000000000L))
                .andExpect(jsonPath("$[0].activityStatus").value("online"));

        verify(connectionsService).getAcceptedConnections("uid_me");
    }

    @Test
    @DisplayName("GET /api/connections/available without userId returns 400")
    void getAvailable_missingUserId_returns400() throws Exception {
        mvc.perform(get("/api/connections/available"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(connectionsService);
    }

    @Test
    @DisplayName("GET /api/connections/available returns available students")
    void getAvailable_returnsAvailableStudents() throws Exception {
        ConnectionsService.ConnectionDTO dto = buildDto(
                "uid_2",
                "Alex",
                "Software Engineering",
                null,
                new String[]{"EECS 3311", "EECS 3221"},
                null,
                "offline"
        );

        when(connectionsService.getAvailableStudents("uid_me")).thenReturn(List.of(dto));

        mvc.perform(get("/api/connections/available").param("userId", "uid_me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value("uid_2"))
                .andExpect(jsonPath("$[0].fullName").value("Alex"))
                .andExpect(jsonPath("$[0].program").value("Software Engineering"))
                .andExpect(jsonPath("$[0].courses[0]").value("EECS 3311"))
                .andExpect(jsonPath("$[0].courses[1]").value("EECS 3221"));

        verify(connectionsService).getAvailableStudents("uid_me");
    }


    @Test
    @DisplayName("GET /api/connections/pending returns pending requests")
    void getPending_returnsPendingRequests() throws Exception {
        ConnectionsService.ConnectionDTO dto = buildDto(
                "uid_sender",
                "Omar",
                "Computer Engineering",
                "",
                new String[]{"EECS 2030"},
                1712000000000L,
                "idle"
        );

        when(connectionsService.getPendingRequests("uid_me")).thenReturn(List.of(dto));

        mvc.perform(get("/api/connections/pending").param("userId", "uid_me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value("uid_sender"))
                .andExpect(jsonPath("$[0].fullName").value("Omar"))
                .andExpect(jsonPath("$[0].activityStatus").value("idle"));

        verify(connectionsService).getPendingRequests("uid_me");
    }

    @Test
    @DisplayName("POST /api/connections/accept calls service and returns success message")
    void acceptRequest_callsService_andReturnsOk() throws Exception {
        String body = """
                {
                  "senderId": "uid_sender",
                  "myUserId": "uid_me"
                }
                """;

        mvc.perform(post("/api/connections/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Request accepted"));

        verify(connectionsService).acceptRequest("uid_sender", "uid_me");
    }

    @Test
    @DisplayName("POST /api/connections/decline calls service and returns success message")
    void declineRequest_callsService_andReturnsOk() throws Exception {
        String body = """
                {
                  "senderId": "uid_sender",
                  "myUserId": "uid_me"
                }
                """;

        mvc.perform(post("/api/connections/decline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Request declined"));

        verify(connectionsService).declineRequest("uid_sender", "uid_me");
    }

    @Test
    @DisplayName("POST /api/connections/remove calls service and returns success message")
    void removeConnection_callsService_andReturnsOk() throws Exception {
        String body = """
                {
                  "myUserId": "uid_me",
                  "targetUserId": "uid_target"
                }
                """;

        mvc.perform(post("/api/connections/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Connection removed"));

        verify(connectionsService).removeConnection("uid_me", "uid_target");
    }
}