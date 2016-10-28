package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totetotetotem on 2016/10/28.
 */

class ItemContainer {
    @SerializedName("Items")
    @Expose
    private List<Item> items = new ArrayList<>();

    List<Item> getItems() {
        return items;
    }
}
