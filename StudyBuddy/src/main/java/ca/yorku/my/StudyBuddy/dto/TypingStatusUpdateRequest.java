package ca.yorku.my.StudyBuddy;

/**
 * Request payload for toggling typing state for the authenticated actor.
 */
public class TypingStatusUpdateRequest {
    private Boolean typing;

    public TypingStatusUpdateRequest() {
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }
}
