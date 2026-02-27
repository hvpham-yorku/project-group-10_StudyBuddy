package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    private final Map<String, Chat> chatStore = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> messageStore = new ConcurrentHashMap<>();

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

        for (Chat existing : chatStore.values()) {
            if (existing.getType() == ChatType.DIRECT &&
                    existing.getParticipantIds().contains(request.getUserA()) &&
                    existing.getParticipantIds().contains(request.getUserB()) &&
                    existing.getParticipantIds().size() == 2) {
                return existing;
            }
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

        chatStore.put(chatId, chat);
        messageStore.putIfAbsent(chatId, new ArrayList<>());
        return chat;
    }

    public Chat createEventChat(String actorId, String eventId, CreateEventChatRequest request)
            throws ExecutionException, InterruptedException {
        if (isBlank(eventId)) {
            throw new ValidationException("eventId is required");
        }
        ensureEventExists(eventId);

        String chatId = "event_" + eventId;
        if (chatStore.containsKey(chatId)) {
            return chatStore.get(chatId);
        }

        List<String> participants = new ArrayList<>();
        if (request != null && request.getParticipantIds() != null) {
            participants.addAll(request.getParticipantIds());
        }
        if (!participants.contains(actorId)) {
            participants.add(actorId);
        }

        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setType(ChatType.EVENT);
        chat.setRelatedId(eventId);
        chat.setParticipantIds(participants);
        chat.setChatName(request != null && !isBlank(request.getChatName()) ? request.getChatName() : "Event Chat");

        chatStore.put(chatId, chat);
        messageStore.putIfAbsent(chatId, new ArrayList<>());
        return chat;
    }

    public MessageResponseDTO sendMessage(String actorId, String chatId, SendMessageDTO request) {
        Chat chat = chatStore.get(chatId);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (request == null || isBlank(request.getContent()) || request.getType() == null) {
            throw new ValidationException("Message content and type are required");
        }
        if (!isBlank(request.getChatId()) && !chatId.equals(request.getChatId())) {
            throw new ValidationException("Path chatId must match payload chatId");
        }

        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setChatId(chatId);
        message.setSenderId(actorId);
        message.setSenderName(actorId);
        message.setContent(request.getContent().trim());
        message.setType(request.getType());
        message.setTimestamp(Instant.now().toString());

        messageStore.computeIfAbsent(chatId, ignored -> new ArrayList<>()).add(message);
        chat.setLastMessage(new LastMessagePreview(actorId, message.getContent(), message.getType(), message.getTimestamp()));

        return toMessageResponse(message, actorId);
    }

    public PagedMessagesResponse getChatMessages(String actorId, String chatId, int limit, String before) {
        Chat chat = chatStore.get(chatId);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (limit < 1 || limit > 100) {
            throw new ValidationException("limit must be between 1 and 100");
        }

        List<Message> all = new ArrayList<>(messageStore.getOrDefault(chatId, new ArrayList<>()));
        all.sort(Comparator.comparing(Message::getTimestamp).reversed());

        int startIndex = 0;
        if (!isBlank(before)) {
            int index = indexOfMessageId(all, before);
            if (index < 0) {
                throw new ValidationException("before cursor does not exist in this chat");
            }
            startIndex = index + 1;
        }

        int endIndex = Math.min(startIndex + limit, all.size());
        List<Message> page = all.subList(startIndex, endIndex);

        PagedMessagesResponse response = new PagedMessagesResponse();
        List<MessageResponseDTO> dtoList = new ArrayList<>();
        for (Message message : page) {
            dtoList.add(toMessageResponse(message, actorId));
        }

        response.setMessages(dtoList);
        response.setHasMore(endIndex < all.size());
        response.setNextCursor(dtoList.isEmpty() ? null : dtoList.get(dtoList.size() - 1).getMessageId());
        return response;
    }

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

    protected void ensureEventExists(String eventId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("events").document(eventId).get().get();
        if (!doc.exists()) {
            throw new NotFoundException("Event not found");
        }
    }

    private MessageResponseDTO toMessageResponse(Message message, String actorId) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setMessageId(message.getMessageId());
        dto.setChatId(message.getChatId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(message.getSenderName());
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setTimestamp(message.getTimestamp());
        dto.setMine(actorId.equals(message.getSenderId()));
        return dto;
    }

    private int indexOfMessageId(List<Message> messages, String messageId) {
        for (int index = 0; index < messages.size(); index++) {
            if (messageId.equals(messages.get(index).getMessageId())) {
                return index;
            }
        }
        return -1;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
