package com.colonolnutty.module.shareddata.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User: Jack's Computer
 * Date: 09/11/2017
 * Time: 12:09 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipe {
    public ItemDescriptor[] input;
    public ItemDescriptor output;
    public String[] groups;
    public boolean excludeFromRecipeBook;
}
