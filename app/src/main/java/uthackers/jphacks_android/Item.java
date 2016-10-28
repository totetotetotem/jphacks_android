package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by totetotetotem on 2016/10/28.
 */

class Item {
    @SerializedName("ItemId")
    @Expose
    private Integer itemId;

    @SerializedName("FamilyId")
    @Expose
    private Integer familyId;

    @SerializedName("ItemName")
    @Expose
    private String itemName;

    @SerializedName("ExpireDate")
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
