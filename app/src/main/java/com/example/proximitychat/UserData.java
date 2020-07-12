package com.example.proximitychat;

public class UserData {
    private String name;
    private String color;

    public UserData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Add an empty constructor so we can later parse JSON into MemberData using Jackson
    public UserData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
