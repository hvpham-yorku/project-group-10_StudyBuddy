package ca.yorku.my.StudyBuddy.services;

import ca.yorku.my.StudyBuddy.StubDatabase;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.services.ConnectionsService.ConnectionDTO;
import ca.yorku.my.StudyBuddy.services.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Profile("stub")
public class StubConnectionsRepository implements ConnectionsRepository {

    private final List<Map<String, String>> connections = new CopyOnWriteArrayList<>();

    @Autowired
    private StudentRepository studentRepo;
    
    @Autowired
    private PresenceRepository presenceRepo;

    @Override
    public List<ConnectionsService.ConnectionDTO> getAcceptedConnections(String myUserId) {
        List<ConnectionsService.ConnectionDTO> out = new ArrayList<>();
        for (Map<String, String> conn : connections) {
            if ("accepted".equals(conn.get("status"))) {
                String otherId = myUserId.equals(conn.get("userA")) ? conn.get("userB") : 
                                 myUserId.equals(conn.get("userB")) ? conn.get("userA") : null;
                if (otherId != null) out.add(buildDTO(otherId));
            }
        }
        return out;
    }

    @Override
    public List<ConnectionsService.ConnectionDTO> getAvailableStudents(String myUserId) {
        List<ConnectionsService.ConnectionDTO> out = new ArrayList<>();
        Set<String> connectedOrPendingIds = new HashSet<>();
        
        for (Map<String, String> conn : connections) {
            if (myUserId.equals(conn.get("userA"))) {
                connectedOrPendingIds.add(conn.get("userB"));
            }
            if (myUserId.equals(conn.get("userB"))) {
                connectedOrPendingIds.add(conn.get("userA"));
            }
        }

        for (Student s : StubDatabase.STUDENTS) {
            if (s.getUserId().equals(myUserId)) continue;
            if (connectedOrPendingIds.contains(s.getUserId())) continue;
            out.add(buildDTO(s.getUserId()));
        }
        return out;
    }

    @Override
    public List<ConnectionsService.ConnectionDTO> getPendingRequests(String myUserId) {
        List<ConnectionsService.ConnectionDTO> out = new ArrayList<>();
        for (Map<String, String> conn : connections) {
            if ("pending".equals(conn.get("status")) && myUserId.equals(conn.get("userB"))) {
                out.add(buildDTO(conn.get("userA")));
            }
        }
        return out;
    }

    @Override
    public void sendRequest(String senderId, String receiverId) {
        Map<String, String> req = new ConcurrentHashMap<>();
        req.put("userA", senderId);
        req.put("userB", receiverId);
        req.put("status", "pending");
        connections.add(req);
    }

    @Override
    public void acceptRequest(String senderId, String myUserId) {
        for (Map<String, String> conn : connections) {
            if ("pending".equals(conn.get("status")) && senderId.equals(conn.get("userA")) && myUserId.equals(conn.get("userB"))) {
                conn.put("status", "accepted");
            }
        }
    }

    @Override
    public void declineRequest(String senderId, String myUserId) {
        connections.removeIf(c -> "pending".equals(c.get("status")) && senderId.equals(c.get("userA")) && myUserId.equals(c.get("userB")));
    }

    @Override
    public void removeConnection(String myUserId, String otherUserId) {
        connections.removeIf(c -> 
            (myUserId.equals(c.get("userA")) && otherUserId.equals(c.get("userB"))) ||
            (otherUserId.equals(c.get("userA")) && myUserId.equals(c.get("userB")))
        );
    }

    private ConnectionsService.ConnectionDTO buildDTO(String userId) {
        ConnectionsService.ConnectionDTO dto = new ConnectionsService.ConnectionDTO();
        dto.userId = userId;
        try {
            Student s = studentRepo.getStudent(userId);
            if (s != null) {
                dto.fullName = s.getFullName();
                dto.program = s.getProgram();
                dto.profilePic = s.getAvatar();
                dto.courses = s.getCourses() != null ? s.getCourses().toArray(new String[0]) : new String[0];
            }
            PresenceService.PresenceRecord p = presenceRepo.getPresenceMap(userId).get(userId);
            if (p != null) dto.lastActiveAt = p.lastActiveAt;
        } catch (Exception ignored) {}
        return dto;
    }

	@Override
	public List<ConnectionDTO> getPendingConnections(String myUserId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ConnectionDTO> getSentRequests(String myUserId) {
		// TODO Auto-generated method stub
		return null;
	}
}