package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class User {
    @SerializedName("access_token")
    @Expose
    private String accessToken;

    String getAccessToken() {
        return accessToken;
    }
}
