package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.daos.ChatDAO;
import ca.yorku.my.StudyBuddy.daos.FriendRequestDAO;
import ca.yorku.my.StudyBuddy.daos.MessageDAO;
import ca.yorku.my.StudyBuddy.dtos.MessageResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.SendFriendRequestDTO;
import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
/**
 * This class implements chat business rules including authorization checks,
 * message validation, friend-request eligibility, and transient typing state.
 */
public class ChatService {

    private static final long TYPING_TTL_MS = 5000L;

    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final FriendRequestDAO friendRequestDAO;
    private final Map<String, Long> typingStateByKey = new ConcurrentHashMap<>();

    @Autowired
    public ChatService(ChatDAO chatDAO, MessageDAO messageDAO, FriendRequestDAO friendRequestDAO) {
        this.chatDAO = chatDAO;
        this.messageDAO = messageDAO;
        this.friendRequestDAO = friendRequestDAO;
    }

    /**
     * Extracts actor identity from the Bearer authorization header.
     */
    public String extractActorId(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Missing Authorization header");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must use Bearer token format");
        }
        String actorId = authorizationHeader.substring(7).trim();
        if (actorId.isEmpty()) {
            throw new UnauthorizedException("Missing actor identity in token");
        }
        return actorId;
    }

    /**
     * Creates or resolves a direct chat while enforcing participant constraints.
     */
    public Chat createDirectChat(String actorId, CreateDirectChatRequest request) {
        if (request == null || isBlank(request.getUserA()) || isBlank(request.getUserB())) {
            throw new ValidationException("Both userA and userB are required");
        }
        if (request.getUserA().equals(request.getUserB())) {
            throw new ValidationException("Direct chat requires two distinct users");
        }
        if (!actorId.equals(request.getUserA()) && !actorId.equals(request.getUserB())) {
            throw new ForbiddenException("Actor must be one of the direct chat participants");
        }

        Chat existing = chatDAO.findDirectByParticipants(request.getUserA(), request.getUserB()).orElse(null);
        if (existing != null) {
            return existing;
        }

        List<String> participants = List.of(request.getUserA(), request.getUserB());
        List<String> sorted = new ArrayList<>(participants);
        sorted.sort(String::compareTo);
        String chatId = sorted.get(0) + "_" + sorted.get(1);

        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setType(ChatType.DIRECT);
        chat.setParticipantIds(new ArrayList<>(participants));
        chat.setChatName("Direct Chat");

        return chatDAO.create(chat);
    }

    /**
     * Creates or resolves an event chat and validates actor/participants against event membership.
     */
//    public Chat createEventChat(String actorId, String eventId, CreateEventChatRequest request)
//            throws ExecutionException, InterruptedException {
//        if (isBlank(eventId)) {
//            throw new ValidationException("eventId is required");
//        }
//
//        Event event = getEventById(eventId);
//
//        Set<String> eligibleParticipants = new HashSet<>();
//        if (event.getParticipantIds() != null) {
//            eligibleParticipants.addAll(event.getParticipantIds());
//        }
//        if (!isBlank(event.getHostId())) {
//            eligibleParticipants.add(event.getHostId());
//        }
//
//        if (!eligibleParticipants.contains(actorId)) {
//            throw new ForbiddenException("Actor must be an event participant");
//        }
//
//        Chat existing = chatDAO.findByRelatedIdAndType(eventId, ChatType.EVENT).orElse(null);
//        if (existing != null) {
//            return existing;
//        }
//
//        String chatId = "event_" + eventId;
//
//        List<String> participants = new ArrayList<>();
//        if (request != null && request.getParticipantIds() != null) {
//            participants.addAll(request.getParticipantIds());
//        }
//        if (participants.isEmpty()) {
//            participants.addAll(eligibleParticipants);
//        }
//
//        if (!participants.contains(actorId)) {
//            participants.add(actorId);
//        }
//
//        for (String participant : participants) {
//            if (!eligibleParticipants.contains(participant)) {
//                throw new ValidationException("All participants must belong to the event");
//            }
//        }
//
//        Chat chat = new Chat();
//        chat.setChatId(chatId);
//        chat.setType(ChatType.EVENT);
//        chat.setRelatedId(eventId);
//        chat.setParticipantIds(participants);
//        chat.setChatName(request != null && !isBlank(request.getChatName()) ? request.getChatName() : "Event Chat");
//
//        return chatDAO.create(chat);
//    }

    /**
     * Validates and persists a chat message, then updates chat preview metadata.
     */
    public MessageResponseDTO sendMessage(String actorId, String chatId, SendMessageDTO request) {
        Chat chat = chatDAO.findById(chatId).orElse(null);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (request == null || request.getType() == null) {
            throw new ValidationException("Message type is required");
        }
        if (!isBlank(request.getChatId()) && !chatId.equals(request.getChatId())) {
            throw new ValidationException("Path chatId must match payload chatId");
        }

        String normalizedContent = normalizeAndValidateMessagePayload(request);

        long nowEpochMillis = Instant.now().toEpochMilli();
        Message message = new Message();
        message.setChatId(chatId);
        message.setSenderId(actorId);
        message.setSenderName(actorId);
        message.setContent(normalizedContent);
        message.setType(request.getType());
        message.setFile(request.getType() == MessageType.FILE ? request.getFile() : null);
        message.setTimestamp(Instant.ofEpochMilli(nowEpochMillis).toString());
        message.setTimestampEpochMillis(nowEpochMillis);

        Message savedMessage = messageDAO.create(chatId, message);
        String previewContent = savedMessage.getContent();
        if (savedMessage.getType() == MessageType.FILE && savedMessage.getFile() != null && !isBlank(savedMessage.getFile().getFileName())) {
            previewContent = "[FILE] " + savedMessage.getFile().getFileName();
        }
        chat.setLastMessage(new LastMessagePreview(actorId, previewContent, savedMessage.getType(), savedMessage.getTimestamp()));
        chatDAO.save(chat);

        return toMessageResponse(savedMessage, actorId);
    }

    /**
     * Validates message payload fields according to message type semantics.
     */
    private String normalizeAndValidateMessagePayload(SendMessageDTO request) {
        if (request.getType() == MessageType.TEXT) {
            if (isBlank(request.getContent())) {
                throw new ValidationException("TEXT message content is required");
            }
            return request.getContent().trim();
        }

        if (request.getType() == MessageType.LINK) {
            if (isBlank(request.getContent())) {
                throw new ValidationException("LINK message content is required");
            }
            String link = request.getContent().trim();
            if (!isValidHttpUrl(link)) {
                throw new ValidationException("LINK content must be a valid http(s) URL");
            }
            return link;
        }

        if (request.getType() == MessageType.FILE) {
            if (request.getFile() == null) {
                throw new ValidationException("FILE metadata is required");
            }
            if (isBlank(request.getFile().getFileName())) {
                throw new ValidationException("FILE fileName is required");
            }
            if (request.getFile().getFileSizeBytes() == null || request.getFile().getFileSizeBytes() <= 0) {
                throw new ValidationException("FILE fileSizeBytes must be greater than 0");
            }
            return isBlank(request.getContent()) ? "" : request.getContent().trim();
        }

        throw new ValidationException("Unsupported message type");
    }

    /**
     * Accepts only fully qualified http(s) URLs for LINK message content.
     */
    private boolean isValidHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && !isBlank(uri.getHost());
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    /**
     * Returns paginated messages using cursor-based fetch semantics.
     */
    public PagedMessagesResponse getChatMessages(String actorId, String chatId, int limit, String before) {
        Chat chat = chatDAO.findById(chatId).orElse(null);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (limit < 1 || limit > 100) {
            throw new ValidationException("limit must be between 1 and 100");
        }

        List<Message> fetched = messageDAO.listMessages(chatId, limit + 1, before);
        boolean hasMore = fetched.size() > limit;
        List<Message> page = hasMore ? fetched.subList(0, limit) : fetched;

        PagedMessagesResponse response = new PagedMessagesResponse();
        List<MessageResponseDTO> dtoList = new ArrayList<>();
        for (Message message : page) {
            dtoList.add(toMessageResponse(message, actorId));
        }

        response.setMessages(dtoList);
        response.setHasMore(hasMore);
        response.setNextCursor(dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).getMessageId());
        return response;
    }

    /**
     * Updates ephemeral typing state for the actor in a chat.
     */
    public void updateTypingStatus(String actorId, String chatId, TypingStatusUpdateRequest request) {
        Chat chat = chatDAO.findById(chatId).orElse(null);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (request == null || request.getTyping() == null) {
            throw new ValidationException("typing is required");
        }

        String key = typingKey(chatId, actorId);
        if (Boolean.TRUE.equals(request.getTyping())) {
            typingStateByKey.put(key, Instant.now().toEpochMilli() + TYPING_TTL_MS);
            return;
        }
        typingStateByKey.remove(key);
    }

    /**
     * Returns currently active typers for a chat and removes expired entries.
     */
    public TypingStatusResponse getTypingStatus(String actorId, String chatId) {
        Chat chat = chatDAO.findById(chatId).orElse(null);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }

        long now = Instant.now().toEpochMilli();
        Map<String, Long> latestExpiryByUser = new HashMap<>();
        List<String> staleKeys = new ArrayList<>();

        // Build a per-user latest expiry snapshot and collect stale entries for cleanup.
        for (Map.Entry<String, Long> entry : typingStateByKey.entrySet()) {
            ParsedTypingKey parsedKey = parseTypingKey(entry.getKey());
            if (parsedKey == null || !chatId.equals(parsedKey.chatId)) {
                continue;
            }

            long expiresAt = entry.getValue() == null ? 0L : entry.getValue();
            if (expiresAt <= now) {
                staleKeys.add(entry.getKey());
                continue;
            }

            if (!parsedKey.userId.equals(actorId)) {
                long existing = latestExpiryByUser.getOrDefault(parsedKey.userId, 0L);
                if (expiresAt > existing) {
                    latestExpiryByUser.put(parsedKey.userId, expiresAt);
                }
            }
        }

        // Opportunistic cleanup keeps memory bounded without a separate scheduler.
        for (String staleKey : staleKeys) {
            typingStateByKey.remove(staleKey);
        }

        // Sort output for deterministic responses (useful for tests and frontend diffs).
        List<Map.Entry<String, Long>> activeEntries = new ArrayList<>(latestExpiryByUser.entrySet());
        activeEntries.sort(Comparator.comparing(Map.Entry::getKey));

        List<String> activeUsers = new ArrayList<>();
        long nearestExpiryMs = TYPING_TTL_MS;
        for (Map.Entry<String, Long> entry : activeEntries) {
            activeUsers.add(entry.getKey());
            long remaining = Math.max(0L, entry.getValue() - now);
            if (remaining < nearestExpiryMs) {
                nearestExpiryMs = remaining;
            }
        }

        TypingStatusResponse response = new TypingStatusResponse();
        response.setTypingUserIds(activeUsers);
        response.setExpiresInMs(activeUsers.isEmpty() ? TYPING_TTL_MS : nearestExpiryMs);
        return response;
    }

    /**
     * Checks whether two users have at least one overlapping attended event.
     *
     * This is used as the eligibility gate for friend-request creation.
     */
    public boolean hasCompletedSharedSession(String userA, String userB) throws ExecutionException, InterruptedException {
        if (isBlank(userA) || isBlank(userB)) {
            throw new ValidationException("userA and userB are required");
        }

        Set<String> userAAttended = new HashSet<>(getAttendedEventIds(userA));
        if (userAAttended.isEmpty()) {
            return false;
        }

        List<String> userBAttended = getAttendedEventIds(userB);
        for (String eventId : userBAttended) {
            if (userAAttended.contains(eventId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a pending friend request once all business rules pass.
     *
     * Rules enforced here:
     * 1) target is present and not self
     * 2) both users exist
     * 3) users share a completed session
     * 4) no existing pending request between the pair
     */
    public FriendRequest sendFriendRequest(String actorId, SendFriendRequestDTO request)
            throws ExecutionException, InterruptedException {
        if (request == null || isBlank(request.getTargetUserId())) {
            throw new ValidationException("targetUserId is required");
        }

        String targetUserId = request.getTargetUserId().trim();
        if (actorId.equals(targetUserId)) {
            throw new ValidationException("Cannot send a friend request to yourself");
        }

        ensureUserExists(actorId);
        ensureUserExists(targetUserId);

        if (!hasCompletedSharedSession(actorId, targetUserId)) {
            throw new ForbiddenException("Users are eligible only after a completed shared session");
        }

        FriendRequest existing = friendRequestDAO.findPendingBetween(actorId, targetUserId).orElse(null);
        if (existing != null) {
            throw new ValidationException("A pending friend request already exists between these users");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestId("fr_" + actorId + "_" + targetUserId);
        friendRequest.setSenderId(actorId);
        friendRequest.setReceiverId(targetUserId);
        friendRequest.setStatus(FriendRequestStatus.PENDING);
        friendRequest.setCreatedAt(Instant.now().toString());

        return friendRequestDAO.create(friendRequest);
    }

    /**
     * Reads attended event ids from the user profile document.
     *
     * Missing user or missing attended list is treated as no attendance.
     */
    protected List<String> getAttendedEventIds(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("users").document(userId).get().get();
        if (!doc.exists()) {
            return new ArrayList<>();
        }

        Student student = doc.toObject(Student.class);
        if (student == null || student.getAttendedEventIds() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(student.getAttendedEventIds());
    }

    /**
     * Loads an event by id and copies Firestore doc id back into the object.
     */
//    protected Event getEventById(String eventId) throws ExecutionException, InterruptedException {
//        Firestore db = FirestoreClient.getFirestore();
//        DocumentSnapshot doc = db.collection("events").document(eventId).get().get();
//        if (!doc.exists()) {
//            throw new NotFoundException("Event not found");
//        }
//
//        Event event = doc.toObject(Event.class);
//        if (event == null) {
//            throw new NotFoundException("Event not found");
//        }
//        event.setEventId(doc.getId());
//        return event;
//    }

    /**
     * Verifies that a user document exists before dependent operations run.
     */
    protected void ensureUserExists(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("users").document(userId).get().get();
        if (!doc.exists()) {
            throw new NotFoundException("User not found");
        }
    }

    /**
     * Maps internal message model to API response shape consumed by frontend.
     */
    private MessageResponseDTO toMessageResponse(Message message, String actorId) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setMessageId(message.getMessageId());
        dto.setChatId(message.getChatId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(message.getSenderName());
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setFile(message.getFile());
        dto.setTimestamp(message.getTimestamp());
        dto.setMine(actorId.equals(message.getSenderId()));
        return dto;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String typingKey(String chatId, String userId) {
        return chatId + ":" + userId;
    }

    /**
     * Parses composite key format <chatId>:<userId>.
     */
    private ParsedTypingKey parseTypingKey(String key) {
        int idx = key.lastIndexOf(':');
        if (idx <= 0 || idx >= key.length() - 1) {
            return null;
        }
        return new ParsedTypingKey(key.substring(0, idx), key.substring(idx + 1));
    }

    private static class ParsedTypingKey {
        private final String chatId;
        private final String userId;

        private ParsedTypingKey(String chatId, String userId) {
            this.chatId = chatId;
            this.userId = userId;
        }
    }
}
