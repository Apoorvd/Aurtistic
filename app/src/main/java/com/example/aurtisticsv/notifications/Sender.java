package com.example.aurtisticsv.notifications;

public class Sender {
    private Data data;
    private String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public Data getData() {
        return this.data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
