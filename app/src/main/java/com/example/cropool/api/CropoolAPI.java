package com.example.cropool.api;

import com.example.cropool.BuildConfig;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CropoolAPI {
    static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
//    @POST("login")
//    Call<LoginPost> login(@Body LoginPost post);

    @POST("register")
    Call<RegisterRes> register(@Body RegisterReq post);


}