package com.example.cropool.api;

import com.example.cropool.BuildConfig;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
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

    @GET("tokens")
    Call<Feedback> tokens(@Header("refresh_token") String refreshToken);

    @GET("accountInfo")
    Call<AccountInfo> accountInfo(@Header("access_token") String accessToken, @Header("firebase_token") String firebaseToken);

    @PATCH("updateInfo")
    Call<Feedback> updateInfo(@Header("access_token") String accessToken, @Header("firebase_token") String firebaseToken, @Body AccountInfo updateInfoReq);

    @PATCH("changePassword")
    Call<Feedback> changePassword(@Header("access_token") String accessToken, @Body UpdatePasswordReq updatePasswordReq);

    @PATCH("logout")
    Call<Feedback> signOut(@Header("access_token") String accessToken);

    @POST("addRoute")
    Call<Feedback> addRoute(@Header("access_token") String accessToken, @Body AddRouteReq addRouteReq);

    @POST("findRoute")
    Call<FindRouteRes> findRoute(@Header("access_token") String accessToken, @Body FindRouteReq findRouteReq);

    @POST("requestCheckpoint")
    Call<Feedback> requestCheckpoint(@Header("access_token") String accessToken, @Body CheckpointReq checkpointReq);

    @GET("myRoutes")
    Call<FindRouteRes> myRoutes(@Header("access_token") String accessToken);

    @GET("subscribedToRoutes")
    Call<FindRouteRes> subscribedToRoutes(@Header("access_token") String accessToken);

    @PATCH("unsubscribeCheckpoint")
    Call<Feedback> unsubscribeCheckpoint(@Header("access_token") String accessToken, @Body CheckpointIDReq checkpointIDReq);

    @PATCH("removeCheckpoint")
    Call<Feedback> removeCheckpoint(@Header("access_token") String accessToken, @Body CheckpointIDReq checkpointIDReq);

    @PATCH("acceptCheckpoint")
    Call<Feedback> acceptCheckpoint(@Header("access_token") String accessToken, @Body CheckpointIDReq checkpointIDReq);

    @POST("requestedCheckpoints")
    Call<RequestedCheckpointsRes> requestedCheckpoints(@Header("access_token") String accessToken, @Body RouteIDReq routeIDReq);

    @PATCH("updateRegistrationToken")
    Call<Feedback> updateRegistrationToken(@Header("access_token") String accessToken, @Header("firebase_token") String firebaseToken, @Body RegIdReq regIdReq);
}