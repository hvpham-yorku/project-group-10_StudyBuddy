package ca.yorku.my.StudyBuddy.services;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ConnectionsService {

    public static class ConnectionDTO {
        public String userId;
        public String fullName;
        public String program;
        public String profilePic;
        public String[] courses;
        public Long lastActiveAt; // epoch ms
        public String activityStatus; // "online" | "idle" | "offline"

        public ConnectionDTO() {}
    }

    // IMPORTANT: Don't initialize Firestore here (can run before FirebaseConfig).
    private Firestore db() {
        return FirestoreClient.getFirestore();
    }

    /**
     * connections/{docId} { userA, userB, status: "accepted" }
     * users/{userId} { fullName, program, profilePic, courses }
     * presence/{userId} { lastActiveAt }
     */
    public List<ConnectionDTO> getAcceptedConnections(String myUserId) {
        List<ConnectionDTO> out = new ArrayList<>();
        if (myUserId == null || myUserId.isBlank()) return out;

        try {
            // Find accepted connections where I am userA OR userB
            List<QueryDocumentSnapshot> docs = new ArrayList<>();

            QuerySnapshot q1 = db().collection("connections")
                    .whereEqualTo("userA", myUserId)
                    .whereEqualTo("status", "accepted")
                    .get().get();
            docs.addAll(q1.getDocuments());

            QuerySnapshot q2 = db().collection("connections")
                    .whereEqualTo("userB", myUserId)
                    .whereEqualTo("status", "accepted")
                    .get().get();
            docs.addAll(q2.getDocuments());

            // Extract the "other userId"s
            Set<String> otherIds = new HashSet<>();
            for (QueryDocumentSnapshot d : docs) {
                String a = d.getString("userA");
                String b = d.getString("userB");
                if (a == null || b == null) continue;
                otherIds.add(a.equals(myUserId) ? b : a);
            }

            long now = System.currentTimeMillis();
            long ONLINE_MS = 2 * 60 * 1000;   // 2 min
            long IDLE_MS   = 10 * 60 * 1000;  // 10 min

            // Fetch user profiles + presence
            for (String otherId : otherIds) {
                ConnectionDTO dto = new ConnectionDTO();
                dto.userId = otherId;

                // users/{userId}
                DocumentSnapshot userDoc = db().collection("users").document(otherId).get().get();
                if (userDoc.exists()) {
                    dto.fullName = userDoc.getString("fullName");
                    dto.program = userDoc.getString("program");
                    dto.profilePic = userDoc.getString("profilePic");
                    dto.courses = toStringArray(userDoc.get("courses"));
                } else {
                    dto.courses = new String[0];
                }

                // presence/{userId}
                DocumentSnapshot presDoc = db().collection("presence").document(otherId).get().get();
                if (presDoc.exists()) {
                    dto.lastActiveAt = presDoc.getLong("lastActiveAt");
                }

                // compute activity status from lastActiveAt
                dto.activityStatus = computeStatus(dto.lastActiveAt, now, ONLINE_MS, IDLE_MS);

                out.add(dto);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return out;
    }

    private String computeStatus(Long lastActiveAt, long now, long ONLINE_MS, long IDLE_MS) {
        if (lastActiveAt == null) return "offline";
        long diff = now - lastActiveAt;
        if (diff <= ONLINE_MS) return "online";
        if (diff <= IDLE_MS) return "idle";
        return "offline";
    }

    private String[] toStringArray(Object obj) {
        if (obj == null) return new String[0];

        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            List<String> strings = new ArrayList<>();
            for (Object x : list) {
                if (x != null) strings.add(x.toString());
            }
            return strings.toArray(new String[0]);
        }

        // Defensive: Firestore usually returns List, but keep this anyway
        if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            String[] out = new String[arr.length];
            for (int i = 0; i < arr.length; i++) out[i] = String.valueOf(arr[i]);
            return out;
        }

        return new String[0];
    }
}