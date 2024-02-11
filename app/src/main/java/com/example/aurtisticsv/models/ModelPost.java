package com.example.aurtisticsv.models;

public class ModelPost {
    String pComments;
    String pDescr;
    String pId;
    String pImage;
    String pLikes;
    String pTime;
    String pTitle;
    String uDp;
    String uEmail;
    String uName;
    String uid;

    public ModelPost(String pId, String pTitle, String pDescr, String pLikes, String pComments, String pImage, String pTime, String uid, String uEmail, String uDp, String uName) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pDescr = pDescr;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    public String getpId() {
        return this.pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return this.pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescr() {
        return this.pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getpLikes() {
        return this.pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return this.pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getpImage() {
        return this.pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return this.pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
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
