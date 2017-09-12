package com.company.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User: Jack's Computer
 * Date: 09/12/2017
 * Time: 11:25 AM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemBase {
    public String itemName;
    public Double price;

    @JsonIgnore
    public Double foodValue;
}