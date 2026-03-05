package ca.yorku.my.StudyBuddy.daos;

import java.util.Optional;

import ca.yorku.my.StudyBuddy.FriendRequest;

/**
 * This class contains persistence contract for friend request records.
 */
public interface FriendRequestDAO {
    /**
     * Creates or updates a friend request record.
     */
    FriendRequest create(FriendRequest request);

    /**
     * Finds an existing pending request between two users in either direction.
     */
    Optional<FriendRequest> findPendingBetween(String userA, String userB);
}
