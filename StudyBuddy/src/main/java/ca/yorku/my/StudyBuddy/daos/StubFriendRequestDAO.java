package ca.yorku.my.StudyBuddy.daos;

import ca.yorku.my.StudyBuddy.FriendRequest;
import ca.yorku.my.StudyBuddy.FriendRequestStatus;
import ca.yorku.my.StudyBuddy.StubDatabase;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("stub")
public class StubFriendRequestDAO implements FriendRequestDAO {
    @Override
    public FriendRequest create(FriendRequest request) {
        request.setRequestId("stub_fr_" + UUID.randomUUID().toString().substring(0, 8));
        StubDatabase.FRIEND_REQUESTS.add(request);
        return request;
    }

    @Override
    public Optional<FriendRequest> findPendingBetween(String userA, String userB) {
        return StubDatabase.FRIEND_REQUESTS.stream()
                .filter(fr -> fr.getStatus() == FriendRequestStatus.PENDING)
                .filter(fr -> (fr.getSenderId().equals(userA) && fr.getReceiverId().equals(userB)) ||
                              (fr.getSenderId().equals(userB) && fr.getReceiverId().equals(userA)))
                .findFirst();
    }
}