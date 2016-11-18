package com.fresh_fridge.uthackers;

import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    @Nullable
    Integer getUserItemId() {
        return userItemId;
    }

    String getItemName() {
        return itemName;
    }

    String getExpireDate() {
        return expireDate;
    }

    @Nullable
    Integer getItemId() {
        return itemId;
    }

    @Nullable
    Integer getExpireDateFromToday() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            Date d = df.parse(this.expireDate);
            c.setTime(d);
            Log.d("expire date", Long.valueOf((c.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60 * 60)).toString());
            Long day = (c.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60 * 60);
            if (24 > day && day > 0) {
                return 1;
            } else {
                return (int) (day / 24);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
