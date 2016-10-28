package uthackers.jphacks_android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by totetotetotem on 2016/10/24.
 */

interface IFoodAPI {

    @GET("/api/")
    Call<FoodContainer> getFoodInfoWithUrl(@Query("url") String familyId);
}
