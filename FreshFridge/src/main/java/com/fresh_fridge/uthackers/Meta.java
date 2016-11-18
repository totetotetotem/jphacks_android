package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Meta {
    @SerializedName("status")
    @Expose
    private Integer status;

    Integer getStatus() {
        return status;
    }
}
