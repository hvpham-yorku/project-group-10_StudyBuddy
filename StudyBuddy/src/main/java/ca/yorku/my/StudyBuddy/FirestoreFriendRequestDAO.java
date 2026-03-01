package ca.yorku.my.StudyBuddy;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreFriendRequestDAO implements FriendRequestDAO {

    private static final String COLLECTION = "friendRequests";

    @Override
    public FriendRequest create(FriendRequest request) {
        if (request == null) {
            throw new ValidationException("friend request is required");
        }
        if (isBlank(request.getRequestId())) {
            throw new ValidationException("requestId is required");
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            db.collection(COLLECTION).document(request.getRequestId()).set(request).get();
            return request;
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save friend request", exception);
        }
    }

    @Override
    public Optional<FriendRequest> findPendingBetween(String userA, String userB) {
        if (isBlank(userA) || isBlank(userB)) {
            return Optional.empty();
        }

        Firestore db = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> forward = db.collection(COLLECTION)
                    .whereEqualTo("senderId", userA)
                    .whereEqualTo("receiverId", userB)
                    .whereEqualTo("status", FriendRequestStatus.PENDING.name())
                    .limit(1)
                    .get();

            Optional<FriendRequest> forwardResult = mapFirst(forward.get().getDocuments());
            if (forwardResult.isPresent()) {
                return forwardResult;
            }

            ApiFuture<QuerySnapshot> reverse = db.collection(COLLECTION)
                    .whereEqualTo("senderId", userB)
                    .whereEqualTo("receiverId", userA)
                    .whereEqualTo("status", FriendRequestStatus.PENDING.name())
                    .limit(1)
                    .get();

            return mapFirst(reverse.get().getDocuments());
        } catch (InterruptedException | ExecutionException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to query friend request", exception);
        }
    }

    private Optional<FriendRequest> mapFirst(List<QueryDocumentSnapshot> documents) {
        if (documents == null || documents.isEmpty()) {
            return Optional.empty();
        }
        QueryDocumentSnapshot document = documents.get(0);
        FriendRequest request = document.toObject(FriendRequest.class);
        if (request == null) {
            return Optional.empty();
        }
        request.setRequestId(document.getId());
        return Optional.of(request);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
