package ca.yorku.my.StudyBuddy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@CrossOrigin(origins = "*")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    // MVP: caller passes userId (later you can swap to auth-derived userId)
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestParam String userId) {
        presenceService.heartbeat(userId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // Example: /api/presence?uids=omar,vaughn
    @GetMapping
    public ResponseEntity<Map<String, PresenceService.PresenceRecord>> getPresence(@RequestParam String uids) {
        return ResponseEntity.ok(presenceService.getPresenceMap(uids));
    }
}