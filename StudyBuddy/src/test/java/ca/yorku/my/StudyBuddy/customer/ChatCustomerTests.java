package ca.yorku.my.StudyBuddy.customer;

import ca.yorku.my.StudyBuddy.StubDatabase;
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
class ChatCustomerTests {

    @Autowired
    private MockMvc mockMvc;

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
        // Customer view: peer typing should appear in a direct chat.
    void customerCanSeePeerTypingIndicatorInDirectChat() throws Exception {
        createDirectChat();

        mockMvc.perform(put("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"typing\":true}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typingUserIds[0]").value("u1"));
    }

    @Test
        // Customer view: users should not see their own typing as remote activity.
    void customerDoesNotReceiveOwnTypingAsRemoteSignal() throws Exception {
        createDirectChat();

        mockMvc.perform(put("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"typing\":true}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chats/u1_u2/typing")
                        .header("Authorization", "Bearer u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typingUserIds").isEmpty());
    }

    @Test
        // Customer view: sync probe indicates when message refetch is needed.
    void customerUsesSyncProbeToKnowWhenToRefetchMessages() throws Exception {
        createDirectChat();

        mockMvc.perform(get("/api/chats/u1_u2/messages/sync")
                        .header("Authorization", "Bearer u1")
                        .param("sinceEpochMillis", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changed").value(false));

        mockMvc.perform(post("/api/chats/u1_u2/messages")
                        .header("Authorization", "Bearer u2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chatId\":\"u1_u2\",\"type\":\"TEXT\",\"content\":\"new message\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/chats/u1_u2/messages/sync")
                        .header("Authorization", "Bearer u1")
                        .param("sinceEpochMillis", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.changed").value(true))
                .andExpect(jsonPath("$.latestMessageId").isNotEmpty());
    }

    private void createDirectChat() throws Exception {
        mockMvc.perform(post("/api/chats/direct")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userA\":\"u1\",\"userB\":\"u2\"}"))
                .andExpect(status().isCreated());
    }
}
