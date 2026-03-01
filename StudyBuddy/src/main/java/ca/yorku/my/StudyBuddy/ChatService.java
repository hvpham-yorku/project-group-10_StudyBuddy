package ca.yorku.my.StudyBuddy;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    private final ChatDAO chatDAO;
    private final MessageDAO messageDAO;
    private final FriendRequestDAO friendRequestDAO;

    @Autowired
    public ChatService(ChatDAO chatDAO, MessageDAO messageDAO, FriendRequestDAO friendRequestDAO) {
        this.chatDAO = chatDAO;
        this.messageDAO = messageDAO;
        this.friendRequestDAO = friendRequestDAO;
    }

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

    public Chat createEventChat(String actorId, String eventId, CreateEventChatRequest request)
            throws ExecutionException, InterruptedException {
        if (isBlank(eventId)) {
            throw new ValidationException("eventId is required");
        }

        Event event = getEventById(eventId);

        Set<String> eligibleParticipants = new HashSet<>();
        if (event.getParticipantIds() != null) {
            eligibleParticipants.addAll(event.getParticipantIds());
        }
        if (!isBlank(event.getHostId())) {
            eligibleParticipants.add(event.getHostId());
        }

        if (!eligibleParticipants.contains(actorId)) {
            throw new ForbiddenException("Actor must be an event participant");
        }

        Chat existing = chatDAO.findByRelatedIdAndType(eventId, ChatType.EVENT).orElse(null);
        if (existing != null) {
            return existing;
        }

        String chatId = "event_" + eventId;

        List<String> participants = new ArrayList<>();
        if (request != null && request.getParticipantIds() != null) {
            participants.addAll(request.getParticipantIds());
        }
        if (participants.isEmpty()) {
            participants.addAll(eligibleParticipants);
        }

        if (!participants.contains(actorId)) {
            participants.add(actorId);
        }

        for (String participant : participants) {
            if (!eligibleParticipants.contains(participant)) {
                throw new ValidationException("All participants must belong to the event");
            }
        }

        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setType(ChatType.EVENT);
        chat.setRelatedId(eventId);
        chat.setParticipantIds(participants);
        chat.setChatName(request != null && !isBlank(request.getChatName()) ? request.getChatName() : "Event Chat");

        return chatDAO.create(chat);
    }

    public MessageResponseDTO sendMessage(String actorId, String chatId, SendMessageDTO request) {
        Chat chat = chatDAO.findById(chatId).orElse(null);
        if (chat == null) {
            throw new NotFoundException("Chat not found");
        }
        if (!chat.getParticipantIds().contains(actorId)) {
            throw new ForbiddenException("Actor is not a participant in this chat");
        }
        if (request == null || isBlank(request.getContent()) || request.getType() == null) {
            throw new ValidationException("Message content and type are required");
        }
        if (request.getType() != MessageType.TEXT) {
            throw new ValidationException("Only TEXT messages are supported in Session 2");
        }
        if (!isBlank(request.getChatId()) && !chatId.equals(request.getChatId())) {
            throw new ValidationException("Path chatId must match payload chatId");
        }

        long nowEpochMillis = Instant.now().toEpochMilli();
        Message message = new Message();
        message.setChatId(chatId);
        message.setSenderId(actorId);
        message.setSenderName(actorId);
        message.setContent(request.getContent().trim());
        message.setType(request.getType());
        message.setTimestamp(Instant.ofEpochMilli(nowEpochMillis).toString());
        message.setTimestampEpochMillis(nowEpochMillis);

        Message savedMessage = messageDAO.create(chatId, message);
        chat.setLastMessage(new LastMessagePreview(actorId, savedMessage.getContent(), savedMessage.getType(), savedMessage.getTimestamp()));
        chatDAO.save(chat);

        return toMessageResponse(savedMessage, actorId);
    }

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

    protected Event getEventById(String eventId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("events").document(eventId).get().get();
        if (!doc.exists()) {
            throw new NotFoundException("Event not found");
        }

        Event event = doc.toObject(Event.class);
        if (event == null) {
            throw new NotFoundException("Event not found");
        }
        event.setEventId(doc.getId());
        return event;
    }

    protected void ensureUserExists(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("users").document(userId).get().get();
        if (!doc.exists()) {
            throw new NotFoundException("User not found");
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
