package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceUnitTests {

    private ChatService newService() {
        return new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO());
    }

    @Test
    void extractActorIdReturnsBearerValue() {
        ChatService service = newService();

        String actorId = service.extractActorId("Bearer u1");

        assertEquals("u1", actorId);
    }

    @Test
    void extractActorIdThrowsWhenMissingHeader() {
        ChatService service = newService();

        assertThrows(UnauthorizedException.class, () -> service.extractActorId(null));
    }

    @Test
    void createDirectChatCreatesStableSortedId() {
        ChatService service = newService();
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
        ChatService service = newService();
        CreateDirectChatRequest request = new CreateDirectChatRequest();
        request.setUserA("u1");
        request.setUserB("u2");

        assertThrows(ForbiddenException.class, () -> service.createDirectChat("u9", request));
    }

    @Test
    void createDirectChatReturnsExistingChatWhenDuplicateRequested() {
        ChatService service = newService();

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
        ChatService service = newService();
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
        ChatService service = newService();
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
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        assertThrows(ValidationException.class,
                () -> service.getChatMessages("u1", "u1_u2", 0, null));
    }

    @Test
    void hasCompletedSharedSessionReturnsTrueWhenUsersShareAttendedEvent() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO());
        service.setAttended("u1", List.of("e1", "e2"));
        service.setAttended("u2", List.of("e3", "e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertTrue(result);
    }

    @Test
    void hasCompletedSharedSessionReturnsFalseWhenNoOverlap() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO());
        service.setAttended("u1", List.of("e1"));
        service.setAttended("u2", List.of("e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertFalse(result);
    }

    @Test
    void createEventChatChecksEventExistsViaServiceBoundary() {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO());
        service.setExistingEvents(Set.of("event-1"));
        service.setEventParticipants("event-1", Set.of("u1", "u2"));

        assertDoesNotThrow(() -> service.createEventChat("u1", "event-1", new CreateEventChatRequest()));
        assertThrows(NotFoundException.class,
                () -> service.createEventChat("u1", "missing-event", new CreateEventChatRequest()));
    }

    @Test
    void createEventChatRejectsParticipantOutsideEvent() {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO());
        service.setExistingEvents(Set.of("event-1"));
        service.setEventParticipants("event-1", Set.of("u1", "u2"));

        CreateEventChatRequest request = new CreateEventChatRequest();
        request.setParticipantIds(Arrays.asList("u1", "u9"));

        assertThrows(ValidationException.class,
                () -> service.createEventChat("u1", "event-1", request));
    }

    @Test
    void sendMessageRejectsNonTextForSession2() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("https://example.com");
        messageRequest.setType(MessageType.LINK);

        assertThrows(ValidationException.class,
                () -> service.sendMessage("u1", "u1_u2", messageRequest));
    }

    @Test
    void getMessagesUsesDescendingCursorPagination() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        for (int index = 1; index <= 3; index++) {
            SendMessageDTO messageRequest = new SendMessageDTO();
            messageRequest.setChatId("u1_u2");
            messageRequest.setContent("m" + index);
            messageRequest.setType(MessageType.TEXT);
            service.sendMessage("u1", "u1_u2", messageRequest);
        }

        PagedMessagesResponse firstPage = service.getChatMessages("u1", "u1_u2", 2, null);
        assertEquals(2, firstPage.getMessages().size());
        assertTrue(firstPage.isHasMore());
        assertNotNull(firstPage.getNextCursor());

        PagedMessagesResponse secondPage = service.getChatMessages("u1", "u1_u2", 2, firstPage.getNextCursor());
        assertEquals(1, secondPage.getMessages().size());
        assertFalse(secondPage.isHasMore());
    }

    private static class TestableChatService extends ChatService {
        private final Map<String, List<String>> attendedByUser = new HashMap<>();
        private Set<String> existingEvents = new HashSet<>();
        private final Map<String, Set<String>> participantsByEvent = new HashMap<>();

        TestableChatService(ChatDAO chatDAO, MessageDAO messageDAO) {
            super(chatDAO, messageDAO);
        }

        void setAttended(String userId, List<String> eventIds) {
            attendedByUser.put(userId, new ArrayList<>(eventIds));
        }

        void setExistingEvents(Set<String> eventIds) {
            existingEvents = new HashSet<>(eventIds);
        }

        void setEventParticipants(String eventId, Set<String> participantIds) {
            participantsByEvent.put(eventId, new HashSet<>(participantIds));
        }

        @Override
        protected List<String> getAttendedEventIds(String userId) {
            return attendedByUser.getOrDefault(userId, new ArrayList<>());
        }

        @Override
        protected Event getEventById(String eventId) {
            if (!existingEvents.contains(eventId)) {
                throw new NotFoundException("Event not found");
            }

            Event event = new Event();
            event.setEventId(eventId);
            event.setHostId("u1");
            event.setParticipantIds(new ArrayList<>(participantsByEvent.getOrDefault(eventId, Set.of("u1"))));
            return event;
        }
    }

    private static class InMemoryChatDAO implements ChatDAO {
        private final Map<String, Chat> storage = new HashMap<>();

        @Override
        public Chat create(Chat chat) {
            storage.put(chat.getChatId(), chat);
            return chat;
        }

        @Override
        public Chat save(Chat chat) {
            storage.put(chat.getChatId(), chat);
            return chat;
        }

        @Override
        public Optional<Chat> findById(String chatId) {
            return Optional.ofNullable(storage.get(chatId));
        }

        @Override
        public Optional<Chat> findDirectByParticipants(String userA, String userB) {
            for (Chat chat : storage.values()) {
                if (chat.getType() == ChatType.DIRECT &&
                        chat.getParticipantIds().size() == 2 &&
                        chat.getParticipantIds().contains(userA) &&
                        chat.getParticipantIds().contains(userB)) {
                    return Optional.of(chat);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Chat> findByRelatedIdAndType(String relatedId, ChatType type) {
            for (Chat chat : storage.values()) {
                if (type == chat.getType() && Objects.equals(relatedId, chat.getRelatedId())) {
                    return Optional.of(chat);
                }
            }
            return Optional.empty();
        }
    }

    private static class InMemoryMessageDAO implements MessageDAO {
        private final Map<String, List<Message>> storage = new HashMap<>();

        @Override
        public Message create(String chatId, Message message) {
            message.setMessageId(UUID.randomUUID().toString());
            storage.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
            return message;
        }

        @Override
        public Message save(String chatId, Message message) {
            storage.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
            return message;
        }

        @Override
        public Optional<Message> findById(String chatId, String messageId) {
            return storage.getOrDefault(chatId, new ArrayList<>())
                    .stream()
                    .filter(message -> messageId.equals(message.getMessageId()))
                    .findFirst();
        }

        @Override
        public List<Message> listMessages(String chatId, int limit, String beforeCursor) {
            List<Message> all = new ArrayList<>(storage.getOrDefault(chatId, new ArrayList<>()));
            all.sort(Comparator.comparingLong(Message::getTimestampEpochMillis).reversed());

            int startIndex = 0;
            if (beforeCursor != null && !beforeCursor.isBlank()) {
                int index = -1;
                for (int cursorIndex = 0; cursorIndex < all.size(); cursorIndex++) {
                    if (beforeCursor.equals(all.get(cursorIndex).getMessageId())) {
                        index = cursorIndex;
                        break;
                    }
                }
                if (index < 0) {
                    throw new ValidationException("before cursor does not exist in this chat");
                }
                startIndex = index + 1;
            }

            int endIndex = Math.min(startIndex + limit, all.size());
            return new ArrayList<>(all.subList(startIndex, endIndex));
        }
    }
}
