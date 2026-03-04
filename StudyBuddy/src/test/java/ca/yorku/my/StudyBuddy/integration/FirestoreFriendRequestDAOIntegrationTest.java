package ca.yorku.my.StudyBuddy.integration;

import ca.yorku.my.StudyBuddy.FriendRequest;
import ca.yorku.my.StudyBuddy.FriendRequestDAO;
import ca.yorku.my.StudyBuddy.FriendRequestStatus;

import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Firestore-backed FriendRequestDAO persistence queries.
 */
@SpringBootTest
class FirestoreFriendRequestDAOIntegrationTest {

    @Autowired
    private FriendRequestDAO friendRequestDAO;

    private String requestId;
    private String senderId;
    private String receiverId;

    @BeforeEach
    void setup() {
        Path firebaseKey = Path.of("src/main/resources/serviceAccountKey.json");
        Assumptions.assumeTrue(Files.exists(firebaseKey),
                "Skipping Firestore friend-request integration tests because serviceAccountKey.json is missing");

        requestId = "it_fr_" + UUID.randomUUID().toString().substring(0, 8);
        senderId = "it_sender_" + requestId.substring(requestId.length() - 4);
        receiverId = "it_receiver_" + requestId.substring(requestId.length() - 4);
    }

    @AfterEach
    void cleanup() throws Exception {
        if (requestId != null) {
            FirestoreClient.getFirestore().collection("friendRequests").document(requestId).delete().get();
        }
    }

    /**
     * Verifies pending friend requests can be created and queried in both user directions.
     */
    @Test
    void createAndFindPendingFriendRequestAgainstActualFirestore() {
        FriendRequest request = new FriendRequest();
        request.setRequestId(requestId);
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setStatus(FriendRequestStatus.PENDING);
        request.setCreatedAt(Instant.now().toString());

        FriendRequest created = friendRequestDAO.create(request);
        assertEquals(requestId, created.getRequestId());

        var forward = friendRequestDAO.findPendingBetween(senderId, receiverId);
        assertTrue(forward.isPresent());
        assertEquals(requestId, forward.get().getRequestId());

        var reverse = friendRequestDAO.findPendingBetween(receiverId, senderId);
        assertTrue(reverse.isPresent());
        assertEquals(requestId, reverse.get().getRequestId());
    }
}
