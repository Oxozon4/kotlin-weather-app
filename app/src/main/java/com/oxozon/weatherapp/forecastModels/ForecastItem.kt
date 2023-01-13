package com.oxozon.weatherapp.forecastModels

data class ForecastItem(
    val clouds: CloudsX,
    val dt: Int,
    val dt_txt: String,
    val main: MainX,
    val pop: Double,
    val rain: RainX,
    val sys: SysX,
    val visibility: Int,
    val weather: List<WeatherX>,
    val wind: WindX
)