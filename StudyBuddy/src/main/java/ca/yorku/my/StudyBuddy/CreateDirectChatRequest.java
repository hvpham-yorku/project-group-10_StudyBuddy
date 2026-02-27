package ca.yorku.my.StudyBuddy;

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
