package ca.yorku.my.StudyBuddy.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("stub")
public class StubPresenceRepository implements PresenceRepository {
    
    private final Map<String, Long> lastActive = new ConcurrentHashMap<>();

    @Override
    public void heartbeat(String userId) {
        if (userId != null && !userId.isBlank()) {
            lastActive.put(userId, System.currentTimeMillis());
        }
    }

    @Override
    public Map<String, PresenceService.PresenceRecord> getPresenceMap(String uidsCsv) {
        Map<String, PresenceService.PresenceRecord> result = new HashMap<>();
        if (uidsCsv == null) return result;
        
        for (String uid : uidsCsv.split(",")) {
            String cleanUid = uid.trim();
            if (!cleanUid.isEmpty()) {
                result.put(cleanUid, new PresenceService.PresenceRecord(lastActive.get(cleanUid)));
            }
        }
        return result;
    }
}