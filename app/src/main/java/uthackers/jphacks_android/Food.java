package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;

/**
 * Created by totetotetotem on 2016/10/28.
 */

class Food {
    @Expose
    private Integer foodId;
    @Expose
    private Integer exposeDay;
    @Expose
    private String foodName;
    @Expose
    private Integer number;

    String getFoodName() {
        return foodName;
    }

    Integer getExposeDay() {
        return exposeDay;
    }

    Integer getNumber() {
        return number;
    }


}
