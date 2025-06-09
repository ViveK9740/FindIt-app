package com.findit.app;

public class FeedItem {
    private String userId;
    private String username;
    private String title;
    private long time;
    private String description;
    private String imageUrl; // This can later be a URL if you're loading from server

    public FeedItem(String userId, String username, String title, long time, String description, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.title = title;
        this.time = time;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {

        return title;
    }

    public long getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
