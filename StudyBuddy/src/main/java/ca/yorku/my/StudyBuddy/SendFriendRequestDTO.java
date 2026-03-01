package ca.yorku.my.StudyBuddy;

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
