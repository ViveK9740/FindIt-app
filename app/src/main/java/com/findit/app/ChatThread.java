package com.findit.app;

public class ChatThread {
    private String userId;
    private String username;
    private String lastMessage;

    public ChatThread() { /* Required empty constructor */ }

    public ChatThread(String userId, String username, String lastMessage) {
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getLastMessage() { return lastMessage; }
}
