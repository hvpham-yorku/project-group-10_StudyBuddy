package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceUnitTests {

    @Test
    void extractActorIdReturnsBearerValue() {
        ChatService service = new ChatService();

        String actorId = service.extractActorId("Bearer u1");

        assertEquals("u1", actorId);
    }

    @Test
    void extractActorIdThrowsWhenMissingHeader() {
        ChatService service = new ChatService();

        assertThrows(UnauthorizedException.class, () -> service.extractActorId(null));
    }

    @Test
    void createDirectChatCreatesStableSortedId() {
        ChatService service = new ChatService();
        CreateDirectChatRequest request = new CreateDirectChatRequest();
        request.setUserA("u2");
        request.setUserB("u1");

        Chat chat = service.createDirectChat("u1", request);

        assertEquals("u1_u2", chat.getChatId());
        assertEquals(ChatType.DIRECT, chat.getType());
        assertEquals(2, chat.getParticipantIds().size());
    }

    @Test
    void createDirectChatBlocksActorOutsideParticipants() {
        ChatService service = new ChatService();
        CreateDirectChatRequest request = new CreateDirectChatRequest();
        request.setUserA("u1");
        request.setUserB("u2");

        assertThrows(ForbiddenException.class, () -> service.createDirectChat("u9", request));
    }

    @Test
    void createDirectChatReturnsExistingChatWhenDuplicateRequested() {
        ChatService service = new ChatService();

        CreateDirectChatRequest first = new CreateDirectChatRequest();
        first.setUserA("u1");
        first.setUserB("u2");

        CreateDirectChatRequest second = new CreateDirectChatRequest();
        second.setUserA("u2");
        second.setUserB("u1");

        Chat created = service.createDirectChat("u1", first);
        Chat duplicate = service.createDirectChat("u2", second);

        assertEquals(created.getChatId(), duplicate.getChatId());
    }

    @Test
    void sendMessageAndGetMessagesHappyPath() {
        ChatService service = new ChatService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("hello world");
        messageRequest.setType(MessageType.TEXT);

        MessageResponseDTO sent = service.sendMessage("u1", "u1_u2", messageRequest);
        PagedMessagesResponse page = service.getChatMessages("u1", "u1_u2", 20, null);

        assertNotNull(sent.getMessageId());
        assertEquals("hello world", sent.getContent());
        assertEquals(1, page.getMessages().size());
        assertEquals("hello world", page.getMessages().get(0).getContent());
        assertFalse(page.isHasMore());
    }

    @Test
    void sendMessageBlocksNonParticipant() {
        ChatService service = new ChatService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("hello");
        messageRequest.setType(MessageType.TEXT);

        assertThrows(ForbiddenException.class,
                () -> service.sendMessage("u3", "u1_u2", messageRequest));
    }

    @Test
    void getMessagesRejectsInvalidLimit() {
        ChatService service = new ChatService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        assertThrows(ValidationException.class,
                () -> service.getChatMessages("u1", "u1_u2", 0, null));
    }

    @Test
    void hasCompletedSharedSessionReturnsTrueWhenUsersShareAttendedEvent() throws Exception {
        TestableChatService service = new TestableChatService();
        service.setAttended("u1", List.of("e1", "e2"));
        service.setAttended("u2", List.of("e3", "e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertTrue(result);
    }

    @Test
    void hasCompletedSharedSessionReturnsFalseWhenNoOverlap() throws Exception {
        TestableChatService service = new TestableChatService();
        service.setAttended("u1", List.of("e1"));
        service.setAttended("u2", List.of("e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertFalse(result);
    }

    @Test
    void createEventChatChecksEventExistsViaServiceBoundary() {
        TestableChatService service = new TestableChatService();
        service.setExistingEvents(Set.of("event-1"));

        assertDoesNotThrow(() -> service.createEventChat("u1", "event-1", new CreateEventChatRequest()));
        assertThrows(NotFoundException.class,
                () -> service.createEventChat("u1", "missing-event", new CreateEventChatRequest()));
    }

    private static class TestableChatService extends ChatService {
        private final Map<String, List<String>> attendedByUser = new HashMap<>();
        private Set<String> existingEvents = new HashSet<>();

        void setAttended(String userId, List<String> eventIds) {
            attendedByUser.put(userId, new ArrayList<>(eventIds));
        }

        void setExistingEvents(Set<String> eventIds) {
            existingEvents = new HashSet<>(eventIds);
        }

        @Override
        protected List<String> getAttendedEventIds(String userId) {
            return attendedByUser.getOrDefault(userId, new ArrayList<>());
        }

        @Override
        protected void ensureEventExists(String eventId) {
            if (!existingEvents.contains(eventId)) {
                throw new NotFoundException("Event not found");
            }
        }
    }
}
