package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import ca.yorku.my.StudyBuddy.model.ChatType;
import ca.yorku.my.StudyBuddy.model.FriendRequestStatus;
import ca.yorku.my.StudyBuddy.model.MessageType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatService business rules using in-memory DAO stubs.
 */
class ChatServiceUnitTests {

    private ChatService newService() {
        return new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
    }

    /**
     * Verifies Bearer header parsing extracts actor id.
     */
    @Test
    void extractActorIdReturnsBearerValue() {
        ChatService service = newService();

        String actorId = service.extractActorId("Bearer u1");

        assertEquals("u1", actorId);
    }

    /**
     * Verifies missing authorization header throws UnauthorizedException.
     */
    @Test
    void extractActorIdThrowsWhenMissingHeader() {
        ChatService service = newService();

        assertThrows(UnauthorizedException.class, () -> service.extractActorId(null));
    }

    /**
     * Verifies direct-chat id generation is stable and sorted.
     */
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

    /**
     * Verifies actors outside the participant pair cannot create the direct chat.
     */
    @Test
    void createDirectChatBlocksActorOutsideParticipants() {
        ChatService service = newService();
        CreateDirectChatRequest request = new CreateDirectChatRequest();
        request.setUserA("u1");
        request.setUserB("u2");

        assertThrows(ForbiddenException.class, () -> service.createDirectChat("u9", request));
    }

    /**
     * Verifies duplicate direct-chat requests return the existing chat.
     */
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

    /**
     * Verifies successful message send and retrieval in paged messages response.
     */
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

    /**
     * Verifies non-participants cannot send messages to a chat.
     */
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

    /**
     * Verifies pagination limit validation rejects out-of-range values.
     */
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

    /**
     * Verifies shared-session check returns true when users attended a common event.
     */
    @Test
    void hasCompletedSharedSessionReturnsTrueWhenUsersShareAttendedEvent() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setAttended("u1", List.of("e1", "e2"));
        service.setAttended("u2", List.of("e3", "e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertTrue(result);
    }

    /**
     * Verifies shared-session check returns false when there is no overlap.
     */
    @Test
    void hasCompletedSharedSessionReturnsFalseWhenNoOverlap() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setAttended("u1", List.of("e1"));
        service.setAttended("u2", List.of("e2"));

        boolean result = service.hasCompletedSharedSession("u1", "u2");

        assertFalse(result);
    }

    /**
     * Verifies event-chat creation checks event existence through service boundary methods.
     */
    @Test
    void createEventChatChecksEventExistsViaServiceBoundary() {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setExistingEvents(Set.of("event-1"));
        service.setEventParticipants("event-1", Set.of("u1", "u2"));

        assertDoesNotThrow(() -> service.createEventChat("u1", "event-1", new CreateEventChatRequest()));
        assertThrows(NotFoundException.class,
                () -> service.createEventChat("u1", "missing-event", new CreateEventChatRequest()));
    }

    /**
     * Verifies event-chat participants must belong to the event roster.
     */
    @Test
    void createEventChatRejectsParticipantOutsideEvent() {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setExistingEvents(Set.of("event-1"));
        service.setEventParticipants("event-1", Set.of("u1", "u2"));

        CreateEventChatRequest request = new CreateEventChatRequest();
        request.setParticipantIds(Arrays.asList("u1", "u9"));

        assertThrows(ValidationException.class,
                () -> service.createEventChat("u1", "event-1", request));
    }

    /**
     * Verifies valid HTTP links are accepted for LINK messages.
     */
    @Test
    void sendMessageAcceptsValidLink() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("https://example.com");
        messageRequest.setType(MessageType.LINK);

        MessageResponseDTO sent = service.sendMessage("u1", "u1_u2", messageRequest);

        assertEquals(MessageType.LINK, sent.getType());
        assertEquals("https://example.com", sent.getContent());
    }

    /**
     * Verifies malformed links are rejected for LINK messages.
     */
    @Test
    void sendMessageRejectsInvalidLinkFormat() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("not-a-valid-link");
        messageRequest.setType(MessageType.LINK);

        assertThrows(ValidationException.class,
                () -> service.sendMessage("u1", "u1_u2", messageRequest));
    }

    /**
     * Verifies FILE messages accept complete file metadata payloads.
     */
    @Test
    void sendMessageAcceptsFilePlaceholderMetadata() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setContent("lecture notes");
        messageRequest.setType(MessageType.FILE);

        FileAttachmentDTO file = new FileAttachmentDTO();
        file.setFileName("notes.pdf");
        file.setFileSizeBytes(2048L);
        file.setMimeType("application/pdf");
        file.setStoragePath("placeholder://notes.pdf");
        messageRequest.setFile(file);

        MessageResponseDTO sent = service.sendMessage("u1", "u1_u2", messageRequest);

        assertEquals(MessageType.FILE, sent.getType());
        assertNotNull(sent.getFile());
        assertEquals("notes.pdf", sent.getFile().getFileName());
        assertEquals(2048L, sent.getFile().getFileSizeBytes());
    }

    /**
     * Verifies FILE messages reject incomplete metadata.
     */
    @Test
    void sendMessageRejectsFileWithoutRequiredMetadata() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        SendMessageDTO messageRequest = new SendMessageDTO();
        messageRequest.setChatId("u1_u2");
        messageRequest.setType(MessageType.FILE);
        FileAttachmentDTO file = new FileAttachmentDTO();
        file.setFileName("");
        file.setFileSizeBytes(0L);
        messageRequest.setFile(file);

        assertThrows(ValidationException.class,
                () -> service.sendMessage("u1", "u1_u2", messageRequest));
    }

    /**
     * Verifies friend-request creation is blocked when users lack shared completed sessions.
     */
    @Test
    void sendFriendRequestRejectsWithoutSharedCompletedSession() {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setAttended("u1", List.of("e1"));
        service.setAttended("u2", List.of("e2"));

        SendFriendRequestDTO request = new SendFriendRequestDTO();
        request.setTargetUserId("u2");

        assertThrows(ForbiddenException.class,
                () -> service.sendFriendRequest("u1", request));
    }

    /**
     * Verifies friend-request creation succeeds when all eligibility checks pass.
     */
    @Test
    void sendFriendRequestCreatesPendingWhenSharedSessionExists() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setAttended("u1", List.of("e1", "e2"));
        service.setAttended("u2", List.of("e2"));

        SendFriendRequestDTO request = new SendFriendRequestDTO();
        request.setTargetUserId("u2");

        FriendRequest created = service.sendFriendRequest("u1", request);

        assertEquals("u1", created.getSenderId());
        assertEquals("u2", created.getReceiverId());
        assertEquals(FriendRequestStatus.PENDING, created.getStatus());
        assertNotNull(created.getCreatedAt());
    }

    /**
     * Verifies cursor pagination returns descending pages and hasMore semantics.
     */
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

    /**
     * Verifies typing status excludes actor and reports other active typers.
     */
    @Test
    void typingStatusIncludesOtherParticipantsAndExcludesActor() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setExistingEvents(Set.of("event-1"));
        service.setEventParticipants("event-1", Set.of("u1", "u2", "u3"));

        CreateEventChatRequest createRequest = new CreateEventChatRequest();
        createRequest.setParticipantIds(List.of("u1", "u2", "u3"));
        Chat chat = service.createEventChat("u1", "event-1", createRequest);

        TypingStatusUpdateRequest typingOn = new TypingStatusUpdateRequest();
        typingOn.setTyping(true);
        service.updateTypingStatus("u1", chat.getChatId(), typingOn);
        service.updateTypingStatus("u2", chat.getChatId(), typingOn);
        service.updateTypingStatus("u3", chat.getChatId(), typingOn);

        TypingStatusResponse response = service.getTypingStatus("u1", chat.getChatId());

        assertEquals(List.of("u2", "u3"), response.getTypingUserIds());
        assertTrue(response.getExpiresInMs() > 0);
        assertTrue(response.getExpiresInMs() <= 5000L);
    }

    /**
     * Verifies typing-off updates remove actor from active typing status.
     */
    @Test
    void typingStatusTurnsOffWhenActorSendsTypingFalse() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        TypingStatusUpdateRequest typingOn = new TypingStatusUpdateRequest();
        typingOn.setTyping(true);
        service.updateTypingStatus("u2", "u1_u2", typingOn);

        TypingStatusUpdateRequest typingOff = new TypingStatusUpdateRequest();
        typingOff.setTyping(false);
        service.updateTypingStatus("u2", "u1_u2", typingOff);

        TypingStatusResponse response = service.getTypingStatus("u1", "u1_u2");

        assertTrue(response.getTypingUserIds().isEmpty());
        assertEquals(5000L, response.getExpiresInMs());
    }

    /**
     * Verifies typing updates require explicit typing flag in payload.
     */
    @Test
    void updateTypingStatusRejectsRequestMissingTypingFlag() {
        ChatService service = newService();
        CreateDirectChatRequest chatRequest = new CreateDirectChatRequest();
        chatRequest.setUserA("u1");
        chatRequest.setUserB("u2");
        service.createDirectChat("u1", chatRequest);

        TypingStatusUpdateRequest invalidRequest = new TypingStatusUpdateRequest();

        assertThrows(ValidationException.class,
                () -> service.updateTypingStatus("u1", "u1_u2", invalidRequest));
    }

    /**
     * Verifies friend requests cannot target the same actor.
     */
    @Test
    void sendFriendRequestRejectsSelfTarget() throws Exception {
        ChatService service = newService();

        SendFriendRequestDTO request = new SendFriendRequestDTO();
        request.setTargetUserId("u1");

        assertThrows(ValidationException.class,
                () -> service.sendFriendRequest("u1", request));
    }

    /**
     * Verifies duplicate pending friend requests are rejected.
     */
    @Test
    void sendFriendRequestRejectsWhenPendingAlreadyExists() throws Exception {
        TestableChatService service = new TestableChatService(new InMemoryChatDAO(), new InMemoryMessageDAO(), new InMemoryFriendRequestDAO());
        service.setAttended("u1", List.of("e1", "e2"));
        service.setAttended("u2", List.of("e2"));

        SendFriendRequestDTO first = new SendFriendRequestDTO();
        first.setTargetUserId("u2");
        service.sendFriendRequest("u1", first);

        SendFriendRequestDTO duplicate = new SendFriendRequestDTO();
        duplicate.setTargetUserId("u2");

        assertThrows(ValidationException.class,
                () -> service.sendFriendRequest("u1", duplicate));
    }

    private static class TestableChatService extends ChatService {
        private final Map<String, List<String>> attendedByUser = new HashMap<>();
        private Set<String> existingEvents = new HashSet<>();
        private final Map<String, Set<String>> participantsByEvent = new HashMap<>();
        private final Set<String> existingUsers = new HashSet<>();

        TestableChatService(ChatDAO chatDAO, MessageDAO messageDAO, FriendRequestDAO friendRequestDAO) {
            super(chatDAO, messageDAO, friendRequestDAO);
            existingUsers.add("u1");
            existingUsers.add("u2");
            existingUsers.add("u3");
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

        @Override
        protected void ensureUserExists(String userId) {
            if (!existingUsers.contains(userId)) {
                throw new NotFoundException("User not found");
            }
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

    private static class InMemoryFriendRequestDAO implements FriendRequestDAO {
        private final Map<String, FriendRequest> storage = new HashMap<>();

        @Override
        public FriendRequest create(FriendRequest request) {
            storage.put(request.getRequestId(), request);
            return request;
        }

        @Override
        public Optional<FriendRequest> findPendingBetween(String userA, String userB) {
            return storage.values().stream()
                    .filter(request -> request.getStatus() == FriendRequestStatus.PENDING)
                    .filter(request ->
                            (request.getSenderId().equals(userA) && request.getReceiverId().equals(userB)) ||
                                    (request.getSenderId().equals(userB) && request.getReceiverId().equals(userA)))
                    .findFirst();
        }
    }
}
