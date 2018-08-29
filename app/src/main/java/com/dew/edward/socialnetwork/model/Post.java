package com.dew.edward.socialnetwork.model;

/**
 * Created by Edward on 8/21/2018.
 */
public class Post {

    private String date;
    private String description;
    private String full_name;
    private String post_image;
    private String profile_image;
    private String time;
    private String uid;

    public Post() {
    }

    public Post(String date, String description, String full_name, String post_image, String profile_image, String time, String uid) {
        this.date = date;
        this.description = description;
        this.full_name = full_name;
        this.post_image = post_image;
        this.profile_image = profile_image;
        this.time = time;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
