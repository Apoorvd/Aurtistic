package com.example.aurtisticsv.models;

public class ModelUser {
    String cover;
    String email;
    String image;
    boolean isBlocked = false;
    String name;
    String onlineStatus;
    String phone;
    String search;
    String typingTo;
    String uid;

    public ModelUser(String name, String email, String search, String phone, String image, String cover, String uid, String onlineStatus, String typingTo, boolean isBlocked) {
        this.name = name;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.isBlocked = isBlocked;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return this.onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return this.typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public boolean isBlocked() {
        return this.isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }
}
