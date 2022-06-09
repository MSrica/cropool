package com.example.cropool.home.messages;

public class Text {
    private final String message, timestamp;
    private final boolean isSentByOtherUser;

    public Text(String message, String time, boolean isSentByOtherUser) {
        this.message = message;
        this.timestamp = time;
        this.isSentByOtherUser = isSentByOtherUser;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isSentByOtherUser() {
        return isSentByOtherUser;
    }
}
