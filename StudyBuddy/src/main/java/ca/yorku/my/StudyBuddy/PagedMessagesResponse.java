package ca.yorku.my.StudyBuddy;

import java.util.ArrayList;
import java.util.List;

public class PagedMessagesResponse {
    private List<MessageResponseDTO> messages = new ArrayList<>();
    private String nextCursor;
    private boolean hasMore;

    public PagedMessagesResponse() {
    }

    public List<MessageResponseDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponseDTO> messages) {
        this.messages = messages;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
