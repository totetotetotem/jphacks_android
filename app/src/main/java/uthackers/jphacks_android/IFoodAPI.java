package uthackers.jphacks_android;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by totetotetotem on 2016/10/24.
 */

interface IFoodAPI {

    @GET("/item/{number}")
    Call<ItemContainer> getFoodInfoWithUrl(@Path("number") String familyId);
}
