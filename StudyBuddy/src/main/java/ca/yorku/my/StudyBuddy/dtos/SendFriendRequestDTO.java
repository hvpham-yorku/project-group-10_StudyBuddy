package ca.yorku.my.StudyBuddy.dtos;

/**
 * This class is a request payload for creating a friend request toward another user.
 */
public class SendFriendRequestDTO {
    private String targetUserId;

    public SendFriendRequestDTO() {
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}
