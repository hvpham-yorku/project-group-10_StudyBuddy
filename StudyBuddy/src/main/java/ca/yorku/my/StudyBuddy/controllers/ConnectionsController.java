package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.ConnectionsRepository;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "*")
public class ConnectionsController {

    private final ConnectionsRepository connectionsService;
    private final AuthRepository authService;

    public ConnectionsController(ConnectionsRepository connectionsService, AuthRepository authService) {
        this.connectionsService = connectionsService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getConnections(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String userId) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (userId != null && !userId.isBlank() && !Objects.equals(userId, callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot request connections for another user");
        }
        return ResponseEntity.ok(connectionsService.getAcceptedConnections(callerId));
    }
    
    @GetMapping("/available")
    public ResponseEntity<?> getAvailable(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String userId) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (userId != null && !userId.isBlank() && !Objects.equals(userId, callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot request students for another user");
        }
        return ResponseEntity.ok(connectionsService.getAvailableStudents(callerId));
    }
    
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        String myUserId = payload.get("myUserId");
        String targetUserId = payload.get("targetUserId");
        if (myUserId == null || !Objects.equals(myUserId, callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid sender identity");
        }
        if (targetUserId == null || targetUserId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid target userId");
        }
        // Validate userId
        if (!userExists(myUserId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid userId");
        }
        connectionsService.sendRequest(myUserId, targetUserId);
        return ResponseEntity.ok("Request sent");
    }

    // Helper method to check user existence
    private boolean userExists(String userId) {
        try {
            // Firestore lookup
            com.google.cloud.firestore.DocumentSnapshot doc = com.google.firebase.cloud.FirestoreClient.getFirestore()
                .collection("students").document(userId).get().get();
            return doc.exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String userId) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (userId != null && !userId.isBlank() && !Objects.equals(userId, callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot request pending list for another user");
        }
        return ResponseEntity.ok(connectionsService.getPendingRequests(callerId));
    }
    
    @PostMapping("/accept")
    public ResponseEntity<String> acceptRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (!Objects.equals(payload.get("myUserId"), callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid receiver identity");
        }
        connectionsService.acceptRequest(payload.get("senderId"), payload.get("myUserId"));
        return ResponseEntity.ok("Request accepted");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineRequest(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (!Objects.equals(payload.get("myUserId"), callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid receiver identity");
        }
        connectionsService.declineRequest(payload.get("senderId"), payload.get("myUserId"));
        return ResponseEntity.ok("Request declined");
    }
    
    @PostMapping("/remove")
    public ResponseEntity<String> removeConnection(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) throws Exception {
        String callerId = authService.verifyFrontendToken(authHeader);
        if (!Objects.equals(payload.get("myUserId"), callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid caller identity");
        }
        connectionsService.removeConnection(payload.get("myUserId"), payload.get("targetUserId"));
        return ResponseEntity.ok("Connection removed");
    }
}