package com.farr.android.farrapp.search;

public class Item {
    public Item(){

    }
    public Item(String name, String category){
        this.name = name;
        this.category = category;
    }
    String getDistance(){
        return "1.23 km";
    }
    String name;
    String category;
}
