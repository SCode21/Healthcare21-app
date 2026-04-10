package com.example.signuploginfirebasee;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ChatMessage {
    private String message;
    private String senderId;
    private @ServerTimestamp Timestamp timestamp;

    public ChatMessage() {} // Needed for Firestore

    public ChatMessage(String message, String senderId) {
        this.message = message;
        this.senderId = senderId;
    }

    public String getMessage() { return message; }
    public String getSenderId() { return senderId; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setMessage(String message) { this.message = message; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}