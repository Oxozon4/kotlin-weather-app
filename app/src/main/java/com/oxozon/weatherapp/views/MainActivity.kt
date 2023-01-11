package com.oxozon.weatherapp.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
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
//import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.oxozon.weatherapp.R
import com.oxozon.weatherapp.models.Constants
import com.oxozon.weatherapp.models.WeatherModel
import com.oxozon.weatherapp.services.WeatherService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager

    //    private var currentLocation: Location? = null
//    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
//    lateinit var apiResponseBody: WeatherModel
    private lateinit var cityName: String
    private lateinit var sharedPref: SharedPreferences

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the Fused location variable
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            // This will redirect you to settings from where you need to turn on the location provider.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "You have denied location permission. Please allow it is mandatory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }

    /**
     * A function which is used to verify that the location or GPS is enable or not of the user's device.
     */
    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * A function to request the current location. Using the fused location provider client.
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    /**
     * A location callback object of fused location provider client where we will get the current location details.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")

            getLocationWeatherDetails(latitude, longitude)
        }
    }

    /**
     * Function is used to get the weather details of the current location based on the latitude longitude
     */
    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {

        if (Constants.isNetworkAvailable(this@MainActivity)) {

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherModel> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherModel> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        val weatherList: WeatherModel? = response.body()
                        if (weatherList != null) {
                            setupUI(weatherList)
                        }
                        Log.i("Response Result", "$weatherList")
                    } else {
                        when (response.code()) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    hideProgressDialog()
                    Log.e("Errorrrrr", t.message.toString())
                }
            })
        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    /**
     * This function is used to dismiss the progress dialog if it is visible to user.
     */
    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(weatherList: WeatherModel) {
        for(i in weatherList.weather.indices) {
            Log.i("Weather Name", weatherList.weather.toString())
            findViewById<TextView>(R.id.tv_main).text = weatherList.weather[i].main
            findViewById<TextView>(R.id.tv_main_description).text = weatherList.weather[i].description
            findViewById<TextView>(R.id.tv_temp).text= weatherList.main.temp.toString() + getUnit(application.resources.configuration.toString())

            findViewById<TextView>(R.id.tv_humidity).text= weatherList.main.humidity.toString() + " %"
            findViewById<TextView>(R.id.tv_min).text = weatherList.main.temp_min.toString() + " min"
            findViewById<TextView>(R.id.tv_max).text = weatherList.main.temp_max.toString() + " max"
            findViewById<TextView>(R.id.tv_speed).text = weatherList.wind.speed.toString()
            findViewById<TextView>(R.id.tv_name).text = weatherList.name
            findViewById<TextView>(R.id.tv_country).text = weatherList.sys.country

            findViewById<TextView>(R.id.tv_sunrise_time).text = unixTime(weatherList.sys.sunrise)
            findViewById<TextView>(R.id.tv_sunset_time).text = unixTime(weatherList.sys.sunset)
        }
    }

    private fun getUnit(value: String): String {
        var value = "°C"
        if (value == "US" || value == "LR" || value == "MM") {
            value = "°F"
        }
        return value
    }

    @SuppressLint("SimpleDateFormat")
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
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
