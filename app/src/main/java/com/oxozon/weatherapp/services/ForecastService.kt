package com.oxozon.weatherapp.services

import com.oxozon.weatherapp.forecastModels.ForecastModel
import com.oxozon.weatherapp.weatherModels.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastService {
    @GET("2.5/forecast")
    fun getWeather(
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("q") q: String?,
        @Query("units") units: String?,
        @Query("appid") appid: String?,
    ): Call<ForecastModel>
}