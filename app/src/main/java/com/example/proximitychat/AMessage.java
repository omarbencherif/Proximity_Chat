package com.example.proximitychat;

import android.text.format.DateUtils;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class AMessage {
    private String text;
    private UserData userData;
    private boolean mine;
    private String timeSent;

    public AMessage(String text, UserData userData, boolean mine) {
        this.text = text;
        this.userData = userData;
        this.mine = mine;
        this.timeSent = Calendar.getInstance().getTime().toString();
    }

    public AMessage(){}

    public String getText() {


        return text;
}

    public UserData getUserData() {
        return userData;
    }

    public boolean isMine() {
        return mine;
    }
}