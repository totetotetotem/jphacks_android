package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.zxing.integration.android.IntentIntegrator;

/**
 * Created by bota on 2016/10/30.
 */

class Meta {

    @SerializedName("status")
    @Expose
    private Integer status;

    public Integer getStatus() {
        return status;
    }
}
