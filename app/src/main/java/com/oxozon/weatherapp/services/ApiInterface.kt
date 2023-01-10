package com.oxozon.weatherapp.services

import com.oxozon.weatherapp.models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("data/2.5/weather?&appid=cc7c254dc3aa9de5fb478224408c6cfb&units=metric")
    fun getCurrentWeather(@Query("q") city: String): Call<WeatherModel>

    // https://api.openweathermap.org/data/2.5/weather?&appid=cc7c254dc3aa9de5fb478224408c6cfb&units=metric&q=Lodz
}