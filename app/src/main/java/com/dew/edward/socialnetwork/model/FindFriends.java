package com.dew.edward.socialnetwork.model;

/**
 * Created by Edward on 8/25/2018.
 */
public class FindFriends {
    private String profile_image;
    private String fullname;
    private String relationshipstatus;

    public FindFriends() {
    }

    public FindFriends(String profileImageString, String fullnameString, String statusString) {
        this.profile_image = profileImageString;
        this.fullname = fullnameString;
        this.relationshipstatus = statusString;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRelationshipstatus() {
        return relationshipstatus;
    }

    public void setRelationshipstatus(String relationshipstatus) {
        this.relationshipstatus = relationshipstatus;
    }
}
