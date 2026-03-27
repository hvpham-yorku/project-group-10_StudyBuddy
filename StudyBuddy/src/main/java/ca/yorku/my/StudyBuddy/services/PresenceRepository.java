package ca.yorku.my.StudyBuddy.services;

import java.util.Map;

public interface PresenceRepository {
    void heartbeat(String userId);
    Map<String, PresenceService.PresenceRecord> getPresenceMap(String uidsCsv);
}