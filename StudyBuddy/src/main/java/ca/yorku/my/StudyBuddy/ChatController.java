package ca.yorku.my.StudyBuddy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.dtos.MessageResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.SendFriendRequestDTO;
import ca.yorku.my.StudyBuddy.dtos.SendMessageDTO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*")
/**
 * This class exposes chat APIs for direct/event conversations, message delivery,
 * friend-request flow, and live typing status indicator.
 */
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Creates or returns a direct chat between two participants.
     */
    @PostMapping("/direct")
    public ResponseEntity<?> createDirectChat(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody CreateDirectChatRequest request) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            Chat chat = chatService.createDirectChat(actorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(chat);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

//    /**
//     * Creates or returns an event chat linked to the given event id.
//     */
//    @PostMapping("/event/{eventId}")
//    public ResponseEntity<?> createEventChat(
//            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
//            @PathVariable String eventId,
//            @RequestBody(required = false) CreateEventChatRequest request) {
//        try {
//            String actorId = chatService.extractActorId(authorizationHeader);
//            Chat chat = chatService.createEventChat(actorId, eventId, request);
//            return ResponseEntity.status(HttpStatus.CREATED).body(chat);
//        } catch (UnauthorizedException ex) {
//            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
//        } catch (ForbiddenException ex) {
//            return error(HttpStatus.FORBIDDEN, ex.getMessage());
//        } catch (NotFoundException ex) {
//            return error(HttpStatus.NOT_FOUND, ex.getMessage());
//        } catch (ValidationException ex) {
//            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
//        } catch (ExecutionException | InterruptedException ex) {
//            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create event chat");
//        } catch (Exception ex) {
//            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
//        }
//    }

    /**
     * Sends a new message to an existing chat.
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String chatId,
            @RequestBody SendMessageDTO request) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            MessageResponseDTO response = chatService.sendMessage(actorId, chatId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

    /**
     * Submits a friend request from the authenticated actor.
     */
    @PostMapping("/friend-requests")
    public ResponseEntity<?> sendFriendRequest(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody SendFriendRequestDTO request) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            FriendRequest response = chatService.sendFriendRequest(actorId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (ExecutionException | InterruptedException ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send friend request");
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

    /**
     * Fetches paged messages for a chat using an optional cursor.
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String chatId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String before) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            PagedMessagesResponse response = chatService.getChatMessages(actorId, chatId, limit, before);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

    /**
     * Updates whether the authenticated actor is currently typing.
     */
    @PutMapping("/{chatId}/typing")
    public ResponseEntity<?> updateTypingStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String chatId,
            @RequestBody TypingStatusUpdateRequest request) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            chatService.updateTypingStatus(actorId, chatId, request);
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

    /**
     * Returns active typing users for the current chat.
     */
    @GetMapping("/{chatId}/typing")
    public ResponseEntity<?> getTypingStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String chatId) {
        try {
            String actorId = chatService.extractActorId(authorizationHeader);
            TypingStatusResponse response = chatService.getTypingStatus(actorId, chatId);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException ex) {
            return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (ForbiddenException ex) {
            return error(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NotFoundException ex) {
            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (ValidationException ex) {
            return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (Exception ex) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
        }
    }

    /**
     * Produces a consistent error payload shape for chat endpoints.
     */
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
