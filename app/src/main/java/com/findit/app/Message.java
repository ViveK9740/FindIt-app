
package com.findit.app;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId, receiverId, message;
    private Timestamp timestamp;

    public Message() {}  // required for Firebase

    public Message(String senderId, String receiverId, String message, Timestamp timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
}
