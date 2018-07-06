package com.journeymate.network;



import com.journeymate.Utils.Constants;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ApiInterface {
    @POST(Constants.LOCATIONDATA)
    Call<ResponseBody> sendLocationData(@HeaderMap HashMap<String, Object> map, @Body HashMap<String, Object> locationData);

    @POST(Constants.SENSORDATA)
    Call<ResponseBody> sendSensorData(@HeaderMap HashMap<String, Object> map, @Body HashMap<String, Object> sensorData);

    @POST(Constants.SENSORDATA)
    Call<ResponseBody> sendSensorDataOld(@Body HashMap<String, Object> sensorData);

    @POST(Constants.SENSORDATARAW)
    Call<ResponseBody> sendRawSensorData(@HeaderMap HashMap<String, Object> map, @Body HashMap<String, Object> sensorData);

    @GET(Constants.GET_WEATHER_FORECAST)
    Call<ResponseBody> getWeatherForecast(@Query("lat") Double lat, @Query("lon") Double lon, @Query("appid") String appId);

}
