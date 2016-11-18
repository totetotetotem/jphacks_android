package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

class ItemContainer {
    @SerializedName("user_item")
    @Expose
    private List<Item> items = new ArrayList<>();
    @SerializedName("meta")
    @Expose
    private Meta meta;

    List<Item> getItems() {
        return items;
    }
}
