package com.example.aurtisticsv.models;

public class ModelGroupChat {
    String message;
    String sender;
    String timestamp;
    String type;

    public ModelGroupChat(String message, String sender, String timestamp, String type) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
