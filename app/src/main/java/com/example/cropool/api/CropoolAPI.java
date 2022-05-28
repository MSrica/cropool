package com.example.cropool.api;

import com.example.cropool.BuildConfig;

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

    @POST("login")
    Call<Feedback> login(@Body LoginReq loginReq);

    @POST("register")
    Call<Feedback> register(@Body RegisterReq registerReq);
}