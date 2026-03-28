package ca.yorku.my.StudyBuddy.integration;

import ca.yorku.my.StudyBuddy.StubDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("stub")
class ChatTypingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        StubDatabase.CHATS.clear();
        StubDatabase.MESSAGES.clear();
        StubDatabase.FRIEND_REQUESTS.clear();
    }

    @AfterEach
    void cleanup() {
        StubDatabase.CHATS.clear();
        StubDatabase.MESSAGES.clear();
        StubDatabase.FRIEND_REQUESTS.clear();
    }

    @Test
    // Ensures a user's typing signal is visible to the other chat participant.
    void typingEndpointReturnsActiveTypersForOtherParticipant() throws Exception {
        createDirectChat("u1", "u2");

        mockMvc.perform(put("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"typing\":true}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typingUserIds[0]").value("u1"))
                .andExpect(jsonPath("$.typingUserIds.length()").value(1))
                .andExpect(jsonPath("$.expiresInMs").isNumber());
    }

    @Test
    // Ensures the API rejects typing updates when the required field is missing.
    void typingEndpointRejectsMissingTypingField() throws Exception {
        createDirectChat("u1", "u2");

        mockMvc.perform(put("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    // Ensures sync endpoint changes from unchanged to changed after posting a message.
    void syncEndpointChangedFlagTransitionsAfterMessageSend() throws Exception {
        createDirectChat("u1", "u2");

        mockMvc.perform(get("/api/chats/u1_u2/messages/sync")
                        .header("Authorization", "Bearer u1")
                        .param("sinceEpochMillis", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changed").value(false))
                .andExpect(jsonPath("$.latestTimestampEpochMillis").value(0));

        mockMvc.perform(post("/api/chats/u1_u2/messages")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"u1_u2\",\"type\":\"TEXT\",\"content\":\"hello sync\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/chats/u1_u2/messages/sync")
                        .header("Authorization", "Bearer u1")
                        .param("sinceEpochMillis", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changed").value(true))
                .andExpect(jsonPath("$.latestMessageId").isNotEmpty());
    }

    private void createDirectChat(String userA, String userB) throws Exception {
        String payload = objectMapper.writeValueAsString(java.util.Map.of("userA", userA, "userB", userB));

        mockMvc.perform(post("/api/chats/direct")
                        .header("Authorization", "Bearer " + userA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }
}
