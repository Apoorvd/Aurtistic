package com.example.aurtisticsv.models;

public class ModelComment {
    String cId;
    String comment;
    String timestamp;
    String uDp;
    String uEmail;
    String uName;
    String uid;

    public ModelComment(String cId, String comment, String timestamp, String uid, String uEmail, String uDp, String uName) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    public String getcId() {
        return this.cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return this.uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return this.uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return this.uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
