package ca.yorku.my.StudyBuddy.services;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PresenceService {

    public static class PresenceRecord {
        public Long lastActiveAt; // epoch ms
        public PresenceRecord() {}
        public PresenceRecord(Long lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    }

    private final Firestore db = FirestoreClient.getFirestore();

    public void heartbeat(String userId) {
        if (userId == null || userId.isBlank()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("lastActiveAt", System.currentTimeMillis());

        db.collection("presence")
          .document(userId)
          .set(data, SetOptions.merge());
    }

    public Map<String, PresenceRecord> getPresenceMap(String uidsCsv) {
        List<String> uids = Arrays.stream(uidsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, PresenceRecord> result = new HashMap<>();
        for (String uid : uids) {
            result.put(uid, new PresenceRecord(null));
        }

        try {
            // Batch-get documents
            List<DocumentReference> refs = uids.stream()
                    .map(uid -> db.collection("presence").document(uid))
                    .collect(Collectors.toList());

            List<DocumentSnapshot> snaps = db.getAll(refs.toArray(new DocumentReference[0])).get();
            for (DocumentSnapshot snap : snaps) {
                if (!snap.exists()) continue;
                Long last = snap.getLong("lastActiveAt");
                result.put(snap.getId(), new PresenceRecord(last));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}