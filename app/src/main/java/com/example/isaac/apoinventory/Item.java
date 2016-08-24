package com.example.isaac.apoinventory;

/**
 * Created by isaac on 2016/08/19.
 */
public class Item {
    private String name;
    private String owner;
    //checkout time/date?

    public Item(String name, String owner){
        this.name = name;
        this.owner = owner;
    }

    public String getOwner(){
        return this.owner;
    }
    public String getName(){
        return this.name;
    }

}
