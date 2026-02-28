package ca.yorku.my.StudyBuddy;

public class Message {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private String timestamp;
    private long timestampEpochMillis;
    private MessageType type;

    public Message() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestampEpochMillis() {
        return timestampEpochMillis;
    }

    public void setTimestampEpochMillis(long timestampEpochMillis) {
        this.timestampEpochMillis = timestampEpochMillis;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
