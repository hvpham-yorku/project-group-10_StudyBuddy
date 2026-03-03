package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

public class TypingStatusResponse {
    private List<String> typingUserIds = new ArrayList<>();
    private long expiresInMs;

    public TypingStatusResponse() {
    }

    public List<String> getTypingUserIds() {
        return typingUserIds;
    }

    public void setTypingUserIds(List<String> typingUserIds) {
        this.typingUserIds = typingUserIds;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
