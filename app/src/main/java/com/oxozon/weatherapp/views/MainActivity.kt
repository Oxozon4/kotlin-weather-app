package com.oxozon.weatherapp.views

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.gson.Gson
import com.oxozon.weatherapp.R
import com.oxozon.weatherapp.models.WeatherModel
import com.oxozon.weatherapp.services.RetrofitInstance
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var apiResponseBody: WeatherModel
    private lateinit var cityName: String
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPref = getPreferences(Context.MODE_PRIVATE)
    }

    private fun getCurrentWeatherData(city: String) {
        val retrofitData = RetrofitInstance.api.getCurrentWeather(city)
        retrofitData.enqueue(object : Callback<WeatherModel?> {
            override fun onResponse(call: Call<WeatherModel?>, response: Response<WeatherModel?>) {
                apiResponseBody = response.body()!! // może się nie zapisywać przy logowaniu
                // aktualizacja widoku
                sharedPref.edit().putString("data", Gson().toJson(apiResponseBody)).apply() // nazwa data - do wyciągania danych
            }

            override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {
                Log.d("MainActivity", "Error")
            }
        })
    }

    private fun readFromFile() {
        val data = sharedPref.getString("api", null)
        // file does not exist
        if (data == null) {
            getCurrentWeatherData(cityName)
            Toast.makeText(
                this@MainActivity,
                "No file saved. Default city ($cityName) is being set.",
                Toast.LENGTH_LONG
            ).show()
            // file exist
        } else {
            val json = JSONObject(data)
            val lastCity = json.getString("name")
            getCurrentWeatherData(lastCity)
            Toast.makeText(
                this@MainActivity,
                "Last location: $lastCity is being set.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}