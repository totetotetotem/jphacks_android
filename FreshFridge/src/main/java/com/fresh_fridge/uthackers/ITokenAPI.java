package com.fresh_fridge.uthackers;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface ITokenAPI {
    @POST("/user/add")
    @Headers("Content-Type: application/json")
    Call<TokenContainer> getToken(@Body PostBody postBody);
}
