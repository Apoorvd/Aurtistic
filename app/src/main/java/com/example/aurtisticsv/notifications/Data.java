package com.example.aurtisticsv.notifications;

public class Data {
    private String body;
    private Integer icon;
    private String notificationType;
    private String sent;
    private String title;
    private String user;

    public Data(String user, String body, String title, String sent, String notificationType, Integer icon) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.notificationType = notificationType;
        this.icon = icon;
    }

    public String getNotificationType() {
        return this.notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return this.sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return this.icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
