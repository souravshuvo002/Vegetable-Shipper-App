package com.sourav.deliveryapp.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("orders")
    private List<Foods> orderList;

    @SerializedName("token")
    private Token token;

    @SerializedName("orderDetails")
    private List<Order> orderDetails;

    @SerializedName("orderItems")
    private List<Order> orderItems;


    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Foods> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Foods> orderList) {
        this.orderList = orderList;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<Order> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<Order> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public List<Order> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<Order> orderItems) {
        this.orderItems = orderItems;
    }
}
