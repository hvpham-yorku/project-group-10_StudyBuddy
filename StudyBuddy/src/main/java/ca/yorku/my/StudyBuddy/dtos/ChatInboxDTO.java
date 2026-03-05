package ca.yorku.my.StudyBuddy.dtos;

/**
 * Lightweight chat list item used for inbox-style views.
 */
public class ChatInboxDTO {
    private String chatId;
    private String chatName;
    private String lastMessagePreview;
    private int unreadCount;

    public ChatInboxDTO() {
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    /**
     * Stores a short preview string (not full message history).
     */
    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
