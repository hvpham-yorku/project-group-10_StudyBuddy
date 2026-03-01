package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

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
}
