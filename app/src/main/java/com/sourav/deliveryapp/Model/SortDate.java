package com.sourav.deliveryapp.Model;

public class SortDate {

    private int id;
    private String name;

    public SortDate() {
    }

    public SortDate(String name) {
        this.name = name;
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
}