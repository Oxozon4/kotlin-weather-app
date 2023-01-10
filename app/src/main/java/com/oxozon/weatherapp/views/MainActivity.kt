package com.oxozon.weatherapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.oxozon.weatherapp.R
import com.oxozon.weatherapp.models.Constants
import com.oxozon.weatherapp.models.WeatherModel
import com.oxozon.weatherapp.services.RetrofitInstance
import com.oxozon.weatherapp.services.WeatherService
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var apiResponseBody: WeatherModel
    private lateinit var cityName: String
    private lateinit var sharedPref: SharedPreferences

    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPref = getPreferences(Context.MODE_PRIVATE)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (!isLocationEnabled()) {
            Toast.makeText(this, "By zagwarantować aktualność danych wymagane jest połączenie internetowe", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity((intent))
        } else {
            Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(this@MainActivity, "By zagwarantować aktualność danych wymagane jest połączenie internetowe", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
            Toast.makeText(this, "Aplikacja pobrała najnowsze dane bazując na Twojej obecnej lokalizacji", Toast.LENGTH_SHORT).show()

        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")
            getLocationWeatherDetails()
        }
    }

    private fun getLocationWeatherDetails() {
        if (Constants.isNetworkAvailable(this)) {
            Toast.makeText(this@MainActivity, "Pomyślnie połączyłeś się z internetem", Toast.LENGTH_SHORT).show()
            val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)


        } else {
            Toast.makeText(this, "Nie nawiązano połaczenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Wygląda na to, że wyłączyłeś rekomendowane usługi internetowe!")
            .setPositiveButton("PRZEJDŹ DO USTAWIEŃ") {
                _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("ODRZUĆ") {
                dialog, _ -> dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

//    private fun getCurrentWeatherData(city: String) {
//        val retrofitData = RetrofitInstance.api.getCurrentWeather(city)
//        retrofitData.enqueue(object : Callback<WeatherModel?> {
//            override fun onResponse(call: Call<WeatherModel?>, response: Response<WeatherModel?>) {
//                apiResponseBody = response.body()!! // może się nie zapisywać przy logowaniu
//                // aktualizacja widoku
//                sharedPref.edit().putString("data", Gson().toJson(apiResponseBody)).apply() // nazwa data - do wyciągania danych
//            }
//
//            override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {
//                Log.d("MainActivity", "Error")
//            }
//        })
//    }

//    private fun readFromFile() {
//        val data = sharedPref.getString("api", null)
//        // file does not exist
//        if (data == null) {
//            getCurrentWeatherData(cityName)
//            Toast.makeText(
//                this@MainActivity,
//                "No file saved. Default city ($cityName) is being set.",
//                Toast.LENGTH_LONG
//            ).show()
//            // file exist
//        } else {
//            val json = JSONObject(data)
//            val lastCity = json.getString("name")
//            getCurrentWeatherData(lastCity)
//            Toast.makeText(
//                this@MainActivity,
//                "Last location: $lastCity is being set.",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
}