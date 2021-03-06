package com.example.earthquakelocateapp;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EarthApi {
    @GET("all_day.geojson")
    Call<Detail> getDetails();
}
