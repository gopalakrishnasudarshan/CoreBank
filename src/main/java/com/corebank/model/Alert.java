package com.corebank.model;

import java.time.LocalDateTime;

public class Alert {

    public enum Status { PENDING, ACKNOWLEDGED }

    private long alertId;
    private Account account;
    private User user;
    private String type;
    private String message;
    private Status status;
    private LocalDateTime createdAt;


    public Alert(Account account, User user, String type, String message, Status status, LocalDateTime createdAt) {
        this.account = account;
        this.user = user;
        this.type = type;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }


    public Alert(long alertId, Account account, User user, String type, String message, Status status, LocalDateTime createdAt) {
        this(account, user, type, message, status, createdAt);
        this.alertId = alertId;
    }


    public long getAlertId() { return alertId; }
    public void setAlertId(long alertId) { this.alertId = alertId; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Alert{" +
                "alertId=" + alertId +
                ", account=" + account +
                ", user=" + user +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
