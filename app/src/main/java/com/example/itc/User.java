package com.example.itc;

import android.net.Uri;

import com.google.firebase.database.Exclude;

public class User {
    private String email;
    private String token;
    private String name;
    private String photoUrl;

    private static User userInstance;

    private User(){

    }

    @Exclude
    public static User getUserInstance(){
        if(userInstance == null){
            userInstance = new User();
        }
        return userInstance;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
