package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class TokenContainer {
    @SerializedName("user")
    @Expose()
    private User user;

    @SerializedName("family")
    @Expose()
    private Family family;

    @SerializedName("meta")
    @Expose()
    private Meta meta;

    User getUser() {
        return user;
    }

    Family getFamily() {
        return family;
    }
}
