package ca.yorku.my.StudyBuddy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}