package uthackers.jphacks_android;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totetotetotem on 2016/10/28.
 */

class FoodContainer {
    @Expose
    private List<Food> foods = new ArrayList<>();

    List<Food> getFoods() {
        return foods;
    }
}
