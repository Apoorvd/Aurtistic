package com.example.aurtisticsv.models;

public class ModelChat {
    boolean isSeen;
    String message;
    String receiver;
    String sender;
    String timestamp;
    String type;

    public ModelChat(String message, String receiver, String sender, String timestamp, String type, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.isSeen = isSeen;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return this.isSeen;
    }

    public void setSeen(boolean seen) {
        this.isSeen = seen;
    }
}
