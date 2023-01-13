package com.oxozon.weatherapp.forecastModels

import com.oxozon.weatherapp.weatherModels.WeatherModel

data class ForecastModel(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<WeatherModel>,
    val message: Int
)