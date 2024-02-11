package com.example.aurtisticsv.models;

public class ModelGroupChatList {
    String createdBy;
    String groupDescription;
    String groupIcon;
    String groupId;
    String groupTitle;
    String timestamp;

    public ModelGroupChatList(String groupId, String groupTitle, String groupDescription, String groupIcon, String timestamp, String createdBy) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDescription = groupDescription;
        this.groupIcon = groupIcon;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return this.groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDescription() {
        return this.groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupIcon() {
        return this.groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
