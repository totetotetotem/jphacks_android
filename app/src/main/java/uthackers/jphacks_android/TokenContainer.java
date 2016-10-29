package uthackers.jphacks_android;

import android.support.design.widget.Snackbar;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by bota on 2016/10/30.
 */

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

    public User getUser() {
        return user;
    }

    public Family getFamily() {
        return family;
    }
}
