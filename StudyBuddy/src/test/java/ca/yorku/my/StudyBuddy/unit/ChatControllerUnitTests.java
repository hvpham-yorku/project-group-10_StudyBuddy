package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import ca.yorku.my.StudyBuddy.model.ChatType;
import ca.yorku.my.StudyBuddy.model.FriendRequestStatus;
import ca.yorku.my.StudyBuddy.model.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChatController request/response behavior and status mapping.
 */
@WebMvcTest(ChatController.class)
class ChatControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

        /**
         * Verifies direct-chat creation returns 201 with created chat payload.
         */
    @Test
    void createDirectChatReturns201() throws Exception {
        CreateDirectChatRequest request = new CreateDirectChatRequest();
        request.setUserA("u1");
        request.setUserB("u2");

        Chat chat = new Chat();
        chat.setChatId("u1_u2");
        chat.setType(ChatType.DIRECT);

        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
        when(chatService.createDirectChat(eq("u1"), any(CreateDirectChatRequest.class))).thenReturn(chat);

        mockMvc.perform(post("/api/chats/direct")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").value("u1_u2"));
    }

        /**
         * Verifies missing authorization is translated to 401 on send-message endpoint.
         */
    @Test
    void sendMessageWithoutAuthReturns401() throws Exception {
        when(chatService.extractActorId(null)).thenThrow(new UnauthorizedException("Missing Authorization header"));

        SendMessageDTO request = new SendMessageDTO();
        request.setChatId("u1_u2");
        request.setContent("hello");
        request.setType(MessageType.TEXT);

        mockMvc.perform(post("/api/chats/u1_u2/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

        /**
         * Verifies non-participant send attempts are translated to 403.
         */
    @Test
    void sendMessageAsNonParticipantReturns403() throws Exception {
        when(chatService.extractActorId("Bearer u3")).thenReturn("u3");
        when(chatService.sendMessage(eq("u3"), eq("u1_u2"), any(SendMessageDTO.class)))
                .thenThrow(new ForbiddenException("Actor is not a participant in this chat"));

        SendMessageDTO request = new SendMessageDTO();
        request.setChatId("u1_u2");
        request.setContent("hello");
        request.setType(MessageType.TEXT);

        mockMvc.perform(post("/api/chats/u1_u2/messages")
                        .header("Authorization", "Bearer u3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

        /**
         * Verifies invalid pagination limit is mapped to 422.
         */
    @Test
    void getMessagesWithInvalidLimitReturns422() throws Exception {
        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
        when(chatService.getChatMessages("u1", "u1_u2", 0, null))
                .thenThrow(new ValidationException("limit must be between 1 and 100"));

        mockMvc.perform(get("/api/chats/u1_u2/messages")
                        .header("Authorization", "Bearer u1")
                        .param("limit", "0"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

        /**
         * Verifies unknown chat id is mapped to 404.
         */
    @Test
    void sendMessageToUnknownChatReturns404() throws Exception {
        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
        when(chatService.sendMessage(eq("u1"), eq("unknown"), any(SendMessageDTO.class)))
                .thenThrow(new NotFoundException("Chat not found"));

        SendMessageDTO request = new SendMessageDTO();
        request.setChatId("unknown");
        request.setContent("hello");
        request.setType(MessageType.TEXT);

        mockMvc.perform(post("/api/chats/unknown/messages")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

        /**
         * Verifies invalid link payloads are mapped to 422.
         */
    @Test
    void sendInvalidLinkMessageReturns422() throws Exception {
        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
        when(chatService.sendMessage(eq("u1"), eq("u1_u2"), any(SendMessageDTO.class)))
                .thenThrow(new ValidationException("LINK content must be a valid http(s) URL"));

        SendMessageDTO request = new SendMessageDTO();
        request.setChatId("u1_u2");
        request.setContent("bad-link");
        request.setType(MessageType.LINK);

        mockMvc.perform(post("/api/chats/u1_u2/messages")
                        .header("Authorization", "Bearer u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

                /**
                 * Verifies valid friend-request submissions return 201.
                 */
        @Test
        void sendFriendRequestReturns201() throws Exception {
                when(chatService.extractActorId("Bearer u1")).thenReturn("u1");

                FriendRequest response = new FriendRequest();
                response.setRequestId("fr_u1_u2");
                response.setSenderId("u1");
                response.setReceiverId("u2");
                response.setStatus(FriendRequestStatus.PENDING);
                response.setCreatedAt("2026-03-01T00:00:00Z");

                when(chatService.sendFriendRequest(eq("u1"), any(SendFriendRequestDTO.class))).thenReturn(response);

                SendFriendRequestDTO request = new SendFriendRequestDTO();
                request.setTargetUserId("u2");

                mockMvc.perform(post("/api/chats/friend-requests")
                                                .header("Authorization", "Bearer u1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.requestId").value("fr_u1_u2"));
        }

        /**
         * Verifies friend-request eligibility failures are mapped to 403.
         */
        @Test
        void sendFriendRequestWithoutSharedSessionReturns403() throws Exception {
                when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
                when(chatService.sendFriendRequest(eq("u1"), any(SendFriendRequestDTO.class)))
                                .thenThrow(new ForbiddenException("Users are eligible only after a completed shared session"));

                SendFriendRequestDTO request = new SendFriendRequestDTO();
                request.setTargetUserId("u2");

                mockMvc.perform(post("/api/chats/friend-requests")
                                                .header("Authorization", "Bearer u1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists());
        }

        /**
         * Verifies typing update endpoint returns 204 on success.
         */
        @Test
        void updateTypingStatusReturns204() throws Exception {
                when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
                doNothing().when(chatService).updateTypingStatus(eq("u1"), eq("u1_u2"), any(TypingStatusUpdateRequest.class));

                TypingStatusUpdateRequest request = new TypingStatusUpdateRequest();
                request.setTyping(true);

                mockMvc.perform(put("/api/chats/u1_u2/typing")
                                                .header("Authorization", "Bearer u1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNoContent());
        }

        /**
         * Verifies typing status retrieval returns active typers and ttl metadata.
         */
        @Test
        void getTypingStatusReturns200() throws Exception {
                when(chatService.extractActorId("Bearer u1")).thenReturn("u1");

                TypingStatusResponse response = new TypingStatusResponse();
                response.setTypingUserIds(java.util.List.of("u2"));
                response.setExpiresInMs(1200L);
                when(chatService.getTypingStatus("u1", "u1_u2")).thenReturn(response);

                mockMvc.perform(get("/api/chats/u1_u2/typing")
                                                .header("Authorization", "Bearer u1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.typingUserIds[0]").value("u2"))
                                .andExpect(jsonPath("$.expiresInMs").value(1200));
        }

        /**
         * Verifies typing-update endpoint returns 401 when auth header is missing.
         */
        @Test
        void updateTypingStatusWithoutAuthReturns401() throws Exception {
                when(chatService.extractActorId(null))
                                .thenThrow(new UnauthorizedException("Missing Authorization header"));

                TypingStatusUpdateRequest request = new TypingStatusUpdateRequest();
                request.setTyping(true);

                mockMvc.perform(put("/api/chats/u1_u2/typing")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").exists());
        }

        /**
         * Verifies typing-update endpoint returns 403 for non-participants.
         */
        @Test
        void updateTypingStatusAsNonParticipantReturns403() throws Exception {
                when(chatService.extractActorId("Bearer u3")).thenReturn("u3");
                doThrow(new ForbiddenException("Actor is not a participant in this chat"))
                                .when(chatService).updateTypingStatus(eq("u3"), eq("u1_u2"), any(TypingStatusUpdateRequest.class));

                TypingStatusUpdateRequest request = new TypingStatusUpdateRequest();
                request.setTyping(true);

                mockMvc.perform(put("/api/chats/u1_u2/typing")
                                                .header("Authorization", "Bearer u3")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists());
        }

        /**
         * Verifies missing typing flag is surfaced as 422 validation error.
         */
        @Test
        void updateTypingStatusWithMissingTypingFlagReturns422() throws Exception {
                when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
                doThrow(new ValidationException("typing is required"))
                                .when(chatService).updateTypingStatus(eq("u1"), eq("u1_u2"), any(TypingStatusUpdateRequest.class));

                TypingStatusUpdateRequest request = new TypingStatusUpdateRequest();

                mockMvc.perform(put("/api/chats/u1_u2/typing")
                                                .header("Authorization", "Bearer u1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.error").exists());
        }
}
