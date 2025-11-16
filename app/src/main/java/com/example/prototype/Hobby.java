package com.example.prototype;

public class Hobby {
    private int id;
    private String name;
    private int streakCount;
    private String lastCheckInDate;

    public Hobby(int id, String name, int streakCount, String lastCheckInDate) {
        this.id = id;
        this.name = name;
        this.streakCount = streakCount;
        this.lastCheckInDate = lastCheckInDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public String getLastCheckInDate() {
        return lastCheckInDate;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public void setLastCheckInDate(String lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }
}
