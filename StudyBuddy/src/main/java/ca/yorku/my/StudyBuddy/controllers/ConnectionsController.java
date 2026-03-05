package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.ConnectionsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "*")
public class ConnectionsController {

    private final ConnectionsService connectionsService;
    

    public ConnectionsController(ConnectionsService connectionsService) {
        this.connectionsService = connectionsService;
    }

    // MVP: /api/connections?userId=omar
    @GetMapping
    public ResponseEntity<List<ConnectionsService.ConnectionDTO>> getConnections(@RequestParam String userId) {
        return ResponseEntity.ok(connectionsService.getAcceptedConnections(userId));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<ConnectionsService.ConnectionDTO>> getAvailable(@RequestParam String userId) throws Exception {
        return ResponseEntity.ok(connectionsService.getAvailableStudents(userId));
    }
    
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody Map<String, String> payload) {
        connectionsService.sendRequest(payload.get("myUserId"), payload.get("targetUserId"));
        return ResponseEntity.ok("Request sent");
    }
}