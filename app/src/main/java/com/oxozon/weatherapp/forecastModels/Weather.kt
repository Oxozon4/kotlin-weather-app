package com.oxozon.weatherapp.forecastModels

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)