package com.example.prototype.model;

public class Mood {
    private int id;
    private String name;
    private String sticker;
    private String timestamp;

    // Constructor for creating a new mood for selection
    public Mood(String name, String sticker) {
        this.name = name;
        this.sticker = sticker;
    }

    // Constructor for loading a mood from the database
    public Mood(int id, String name, String sticker, String timestamp) {
        this.id = id;
        this.name = name;
        this.sticker = sticker;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSticker() {
        return sticker;
    }

    public void setSticker(String sticker) {
        this.sticker = sticker;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
