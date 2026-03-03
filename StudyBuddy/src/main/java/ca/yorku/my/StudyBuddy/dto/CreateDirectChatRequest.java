package ca.yorku.my.StudyBuddy;

/**
 * Request payload for creating or resolving a direct chat between two users.
 */
public class CreateDirectChatRequest {
    private String userA;
    private String userB;

    public CreateDirectChatRequest() {
    }

    public String getUserA() {
        return userA;
    }

    public void setUserA(String userA) {
        this.userA = userA;
    }

    public String getUserB() {
        return userB;
    }

    public void setUserB(String userB) {
        this.userB = userB;
    }
}
