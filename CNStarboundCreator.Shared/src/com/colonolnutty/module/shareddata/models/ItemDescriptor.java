package com.colonolnutty.module.shareddata.models;

/**
 * User: Jack's Computer
 * Date: 09/11/2017
 * Time: 12:14 PM
 */
public class ItemDescriptor {
    public String item;
    public Double count;

    //This is here for when attempting to load this class from JSON
    public ItemDescriptor() {}

    public ItemDescriptor(String item, Double count) {
        this.item = item;
        this.count = count;
    }
}
