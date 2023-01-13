package com.oxozon.weatherapp.services

import com.oxozon.weatherapp.models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("q") q: String?,
        @Query("units") units: String?,
        @Query("appid") appid: String?,
        ) : Call<WeatherModel>
}