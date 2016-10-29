package uthackers.jphacks_android;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by totetotetotem on 2016/10/28.
 */

class Item {
    @SerializedName("user_item_id")
    @Expose
    private Integer userItemId;

    @SerializedName("item_id")
    @Expose
    @Nullable
    private Integer itemId;

    @SerializedName("item_name")
    @Expose
    private String itemName;

    @SerializedName("expire_date")
    @Expose
    private String expireDate;

    String getItemName() {
        return itemName;
    }

    String getExpireDate() {
        return expireDate;
    }

    Integer getItemId() {
        return itemId;
    }


}
