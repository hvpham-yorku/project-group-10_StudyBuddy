package ca.yorku.my.StudyBuddy;

import java.util.Optional;

public interface FriendRequestDAO {
    FriendRequest create(FriendRequest request);
    Optional<FriendRequest> findPendingBetween(String userA, String userB);
}
