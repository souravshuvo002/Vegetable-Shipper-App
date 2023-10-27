package com.sourav.deliveryapp.Model;

public class Token {

    private String id, username, token, isServerToken;

    public Token() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIsServerToken() {
        return isServerToken;
    }

    public void setIsServerToken(String isServerToken) {
        this.isServerToken = isServerToken;
    }
}