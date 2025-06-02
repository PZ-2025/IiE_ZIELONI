package com.example.projektzielonifx.models;

import java.time.LocalDateTime;

public class Notification {
    protected int id;
    protected String message;
    protected String type;
    protected LocalDateTime createdAt;
    protected boolean isRead;

    public Notification(int id, String message, String type, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.isRead = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + message;
    }
}
