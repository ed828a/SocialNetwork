package com.dew.edward.socialnetwork.model;

/**
 * Created by Edward on 8/28/2018.
 */
public class Friends {
    String date;

    public Friends() {
    }

    public Friends(String date, String friendName, String friendProfileImage) {
        this.date = date;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
