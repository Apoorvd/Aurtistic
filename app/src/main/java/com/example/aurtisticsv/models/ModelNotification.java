package com.example.aurtisticsv.models;

public class ModelNotification {
    String notification;
    String pId;
    String pUid;
    String sEmail;
    String sImage;
    String sName;
    String sUid;
    String timestamp;

    public ModelNotification(String pId, String timestamp, String pUid, String notification, String sUid, String sName, String sEmail, String sImage) {
        this.pId = pId;
        this.timestamp = timestamp;
        this.pUid = pUid;
        this.notification = notification;
        this.sUid = sUid;
        this.sName = sName;
        this.sEmail = sEmail;
        this.sImage = sImage;
    }

    public String getpId() {
        return this.pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getpUid() {
        return this.pUid;
    }

    public void setpUid(String pUid) {
        this.pUid = pUid;
    }

    public String getNotification() {
        return this.notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getsUid() {
        return this.sUid;
    }

    public void setsUid(String sUid) {
        this.sUid = sUid;
    }

    public String getsName() {
        return this.sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsEmail() {
        return this.sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsImage() {
        return this.sImage;
    }

    public void setsImage(String sImage) {
        this.sImage = sImage;
    }
}
