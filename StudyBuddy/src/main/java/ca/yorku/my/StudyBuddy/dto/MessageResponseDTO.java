package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.model.MessageType;

/**
 * This class contains logic for when response payload is returned after sending/fetching chat messages.
 */
public class MessageResponseDTO {
    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private String timestamp;
    private MessageType type;
    private FileAttachmentDTO file;
    private boolean isMine;

    public MessageResponseDTO() {
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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public FileAttachmentDTO getFile() {
        return file;
    }

    public void setFile(FileAttachmentDTO file) {
        this.file = file;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
