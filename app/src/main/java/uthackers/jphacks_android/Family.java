package uthackers.jphacks_android;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by bota on 2016/10/30.
 */

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
