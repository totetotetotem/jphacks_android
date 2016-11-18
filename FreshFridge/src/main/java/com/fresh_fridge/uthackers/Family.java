package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Family {
    @SerializedName("token")
    @Expose
    private String token;

    Family(String token) {
        this.token = token;
    }

    String getToken() {
        return token;
    }
}
