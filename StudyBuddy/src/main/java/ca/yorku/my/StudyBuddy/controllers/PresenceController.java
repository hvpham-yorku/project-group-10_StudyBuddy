package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import ca.yorku.my.StudyBuddy.services.AuthRepository;
import ca.yorku.my.StudyBuddy.services.PresenceRepository;
import ca.yorku.my.StudyBuddy.services.PresenceService;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/presence")
@CrossOrigin(origins = "*")
public class PresenceController {

    private final PresenceRepository presenceService;
    private final AuthRepository authService;

    public PresenceController(PresenceRepository presenceService, AuthRepository authService) {
        this.presenceService = presenceService;
        this.authService = authService;
    }

    // MVP: caller passes userId (later you can swap to auth-derived userId)
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String userId) throws Exception {
        // Security fix (ID-62/ID-86): token subject is the only identity allowed for heartbeat.
        String callerId = authService.verifyFrontendToken(authHeader);
        if (userId != null && !userId.isBlank() && !Objects.equals(userId, callerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot heartbeat for another user"));
        }
        presenceService.heartbeat(callerId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // Example: /api/presence?uids=omar,vaughn
    @GetMapping
    public ResponseEntity<Map<String, PresenceService.PresenceRecord>> getPresence(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String uids) throws Exception {
        // Security fix (ID-62): block unauthenticated presence scraping.
        authService.verifyFrontendToken(authHeader);
        return ResponseEntity.ok(presenceService.getPresenceMap(uids));
    }
}