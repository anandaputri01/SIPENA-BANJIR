package models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int reportId;
    private int senderId;
    private String message;
    private boolean isAdminMessage;
    private LocalDateTime createdAt;

    // Constructor
    public Message(int id, int reportId, int senderId, String message, boolean isAdminMessage, LocalDateTime createdAt) {
        this.id = id;
        this.reportId = reportId;
        this.senderId = senderId;
        this.message = message;
        this.isAdminMessage = isAdminMessage;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAdminMessage() {
        return isAdminMessage;
    }

    public void setAdminMessage(boolean adminMessage) {
        isAdminMessage = adminMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}