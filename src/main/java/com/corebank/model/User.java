package com.corebank.model;

import java.time.LocalDateTime;

public class User {

    public enum Role { ADMIN, TELLER, MANAGER }

    private long userId;
    private String userName;
    private String password;
    private Role role;
    private LocalDateTime createdAt;

    public User(long userId, String userName, String password, Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }


    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
