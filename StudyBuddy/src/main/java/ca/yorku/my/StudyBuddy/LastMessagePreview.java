package ca.yorku.my.StudyBuddy;

public class LastMessagePreview {
    private String senderId;
    private String content;
    private MessageType type;
    private String timestamp;

    public LastMessagePreview() {
    }

    public LastMessagePreview(String senderId, String content, MessageType type, String timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
