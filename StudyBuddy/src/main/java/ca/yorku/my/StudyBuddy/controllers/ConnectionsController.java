package ca.yorku.my.StudyBuddy.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.yorku.my.StudyBuddy.services.ConnectionsRepository;
import ca.yorku.my.StudyBuddy.services.ConnectionsService.ConnectionDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@CrossOrigin(origins = "*")
public class ConnectionsController {

    private final ConnectionsRepository connectionsService;

    public ConnectionsController(ConnectionsRepository connectionsService) {
        this.connectionsService = connectionsService;
    }

    @GetMapping
    public ResponseEntity<List<ConnectionDTO>> getConnections(@RequestParam String userId) throws Exception {
        return ResponseEntity.ok(connectionsService.getAcceptedConnections(userId));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<ConnectionDTO>> getAvailable(@RequestParam String userId) throws Exception {
        return ResponseEntity.ok(connectionsService.getAvailableStudents(userId));
    }
    
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody Map<String, String> payload) throws Exception {
        connectionsService.sendRequest(payload.get("myUserId"), payload.get("targetUserId"));
        return ResponseEntity.ok("Request sent");
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionDTO>> getPending(@RequestParam String userId) throws Exception {
        return ResponseEntity.ok(connectionsService.getPendingRequests(userId));
    }
    
    @PostMapping("/accept")
    public ResponseEntity<String> acceptRequest(@RequestBody Map<String, String> payload) throws Exception {
        connectionsService.acceptRequest(payload.get("senderId"), payload.get("myUserId"));
        return ResponseEntity.ok("Request accepted");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineRequest(@RequestBody Map<String, String> payload) throws Exception {
        connectionsService.declineRequest(payload.get("senderId"), payload.get("myUserId"));
        return ResponseEntity.ok("Request declined");
    }
    
    @PostMapping("/remove")
    public ResponseEntity<String> removeConnection(@RequestBody Map<String, String> payload) throws Exception {
        connectionsService.removeConnection(payload.get("myUserId"), payload.get("targetUserId"));
        return ResponseEntity.ok("Connection removed");
    }
}