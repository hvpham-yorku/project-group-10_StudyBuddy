package ca.yorku.my.StudyBuddy.services;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Profile("firestore")
public class PresenceService implements PresenceRepository {

    public static class PresenceRecord {
        public Long lastActiveAt; // epoch ms
        public PresenceRecord() {}
        public PresenceRecord(Long lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    }

    private final Firestore db = FirestoreClient.getFirestore();

    @Override
    public void heartbeat(String userId) {
        if (userId == null || userId.isBlank()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("lastActiveAt", System.currentTimeMillis());

        db.collection("presence")
          .document(userId)
          .set(data, SetOptions.merge());
    }

    @Override
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

            List<DocumentSnapshot> snaps = db.getAll(refs.toArray(DocumentReference[]::new)).get();
            for (DocumentSnapshot snap : snaps) {
                if (!snap.exists()) continue;
                Long last = snap.getLong("lastActiveAt");
                result.put(snap.getId(), new PresenceRecord(last));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result;
        } catch (ExecutionException e) {
            return result;
        }

        return result;
    }
}