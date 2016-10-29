package uthackers.jphacks_android;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by bota on 2016/10/30.
 */

interface ITokenAPI {
    @POST("/user/add")
    @Headers("Content-Type: application/json")
    Call<TokenContainer> getToken();
}
