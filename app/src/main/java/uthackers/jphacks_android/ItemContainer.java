package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totetotetotem on 2016/10/28.
 */

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
