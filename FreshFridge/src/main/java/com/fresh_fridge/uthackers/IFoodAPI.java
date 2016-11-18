package com.fresh_fridge.uthackers;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface IFoodAPI {
    @GET("/item/list")
    Call<ItemContainer> getItemList(@Header("x-access-token") String token);

    @POST("/item/delete")
    Call<ResultContainer> postDelete(@Header("x-access-token") String token, @Body ItemDeleteRequest params);
}
