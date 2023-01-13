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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.oxozon.weatherapp.R
import com.oxozon.weatherapp.weatherModels.WeatherModel
import com.oxozon.weatherapp.services.WeatherService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.oxozon.weatherapp.forecastModels.ForecastModel
import com.oxozon.weatherapp.services.ForecastService

class MainActivity : AppCompatActivity() {
    // Const
    private val appId: String = "cc7c254dc3aa9de5fb478224408c6cfb"
    private val baseUrl: String = "https://api.openweathermap.org/data/"
    private val preferenceName: String = "WeatherAppPreference"
    private val weatherData: String = "weather_response_data"
    private val forecastData: String = "forecast_response_data"

    // fragments
    private lateinit var firstFragment: FirstFragment
    private lateinit var secondFragment: SecondFragment
    private lateinit var thirdFragment: ThirdFragment

    // variables
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private var chosenMeasurementUnit: String = "metric"
    private var selectedCity: String? = null
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firstFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as FirstFragment
        secondFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as SecondFragment
        thirdFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView3) as ThirdFragment

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        setBtnClickListener()
        displayPermissionMessage()
    }

    private fun setBtnClickListener() {
        val searchButton = findViewById<Button>(R.id.searchButton)

        searchButton.setOnClickListener {
            val inputField = findViewById<TextInputEditText>(R.id.cityInput)
            val inputFieldValue = inputField.text.toString()
            if (inputFieldValue != "") {
                selectedCity = inputFieldValue
                getLocationWeatherDetails(null, null, inputFieldValue)
                getLocationWeatherForecast(null, null, inputFieldValue)
            }
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(inputField.windowToken, 0)
        }
    }

    private fun displayPermissionMessage() {
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

    fun onFirstFragmentCreated() {
        Log.d("test", "first fragmentCreated")
    }

    fun onSecondFragmentCreated() {
        Log.d("test", "second fragment Created")
    }

    fun onThirdFragmentCreated() {
        Log.d("test", "third fragment Created")
        setupUI()
        setupForecastUI()
    }

    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

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

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            val longitude = mLastLocation.longitude
            userLatitude = latitude
            userLongitude = longitude
            Log.i("Current Latitude", "$latitude")
            Log.i("Current Longitude", "$longitude")

            getLocationWeatherDetails(latitude, longitude, null)
            getLocationWeatherForecast(latitude, longitude, null)
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }

    private fun getLocationWeatherDetails(latitude: Double?, longitude: Double?, city: String?) {

        if (isNetworkAvailable(this@MainActivity) && city == null) {

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherModel> = service.getWeather(
                latitude, longitude, null, chosenMeasurementUnit, appId
            )

            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherModel> {
                @SuppressLint("SetTextI18n", "CommitPrefEdits")
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        val weatherList: WeatherModel? = response.body()

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(weatherData, weatherResponseJsonString)
                        editor.apply()
                        setupUI()

                        Log.i("Response Result", "$weatherList")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "There was an error with your request",
                            Toast.LENGTH_SHORT
                        ).show()
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
                }
            })
        } else if (city != "") {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherModel> = service.getWeather(
                null, null, city, chosenMeasurementUnit, appId
            )
            Log.d("test2", listCall.toString())
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherModel> {
                @SuppressLint("SetTextI18n", "CommitPrefEdits")
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        val weatherList: WeatherModel? = response.body()

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(weatherData, weatherResponseJsonString)
                        editor.apply()
                        setupUI()

                        Log.i("Response Result", "$weatherList")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "There was an error with your request",
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun getLocationWeatherForecast(latitude: Double?, longitude: Double?, city: String?) {

        if (isNetworkAvailable(this@MainActivity) && city == null) {

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: ForecastService =
                retrofit.create<ForecastService>(ForecastService::class.java)

            val listCall: Call<ForecastModel> = service.getWeather(
                latitude, longitude, null, chosenMeasurementUnit, appId
            )

            listCall.enqueue(object : Callback<ForecastModel> {
                @SuppressLint("SetTextI18n", "CommitPrefEdits")
                override fun onResponse(
                    call: Call<ForecastModel>,
                    response: Response<ForecastModel>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        val weatherList: ForecastModel? = response.body()

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(forecastData, weatherResponseJsonString)
                        editor.apply()
                        setupForecastUI()

                        Log.i("Response Result", "$weatherList")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "There was an error with your request",
                            Toast.LENGTH_SHORT
                        ).show()
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

                override fun onFailure(call: Call<ForecastModel>, t: Throwable) {
                    hideProgressDialog()
                }
            })
        } else if (city != "") {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: ForecastService =
                retrofit.create<ForecastService>(ForecastService::class.java)

            val listCall: Call<ForecastModel> = service.getWeather(
                null, null, city, chosenMeasurementUnit, appId
            )
            Log.d("test2", listCall.toString())
            listCall.enqueue(object : Callback<ForecastModel> {
                @SuppressLint("SetTextI18n", "CommitPrefEdits")
                override fun onResponse(
                    call: Call<ForecastModel>,
                    response: Response<ForecastModel>
                ) {
                    if (response.isSuccessful) {
                        hideProgressDialog()
                        val forecastList: ForecastModel? = response.body()

                        val forecastResponseJsonString = Gson().toJson(forecastList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(forecastData, forecastResponseJsonString)
                        editor.apply()
                        setupForecastUI()

                        Log.i("Response Result", "$forecastList")
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "There was an error with your request",
                            Toast.LENGTH_SHORT
                        ).show()
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

                override fun onFailure(call: Call<ForecastModel>, t: Throwable) {
                    hideProgressDialog()
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
        mProgressDialog!!.setContentView(R.layout.progress_dialog)
        mProgressDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_refresh -> {
                if (selectedCity != null && selectedCity.toString() != "") {
                    getLocationWeatherDetails(null, null, selectedCity)
                    getLocationWeatherForecast(null, null, selectedCity)
                } else if (userLatitude != null && userLongitude != null) {
                    getLocationWeatherDetails(userLatitude, userLongitude, null)
                    getLocationWeatherForecast(userLatitude, userLongitude, null)
                } else {
                    requestLocationData()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val weatherResponseJsonString = mSharedPreferences.getString(weatherData, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {
            val weatherList = Gson().fromJson(weatherResponseJsonString, WeatherModel::class.java)
            for (i in weatherList.weather.indices) {
                firstFragment.tvCity.text = weatherList.name
                firstFragment.tvCountry.text = weatherList.sys.country

                firstFragment.tvLatitude.text = "lat: " + weatherList.coord.lat.toString()
                firstFragment.tvLongitude.text = "lon: " + weatherList.coord.lon.toString()

                firstFragment.tvMain.text = weatherList.weather[i].main
                firstFragment.tvMainDescription.text = weatherList.weather[i].description

                when (weatherList.weather[i].description) {
                    "Clear" -> firstFragment.ivMain.setImageResource(R.drawable.sun_vector)
                    "Thunderstorm" -> firstFragment.ivMain.setImageResource(R.drawable.storm_vector)
                    "Rain", "Drizzle" -> firstFragment.ivMain.setImageResource(R.drawable.water_vector)
                    "Clouds" -> firstFragment.ivMain.setImageResource(R.drawable.cloud_vector)
                    "Snow" -> firstFragment.ivMain.setImageResource(R.drawable.snow_vector)
                    else -> firstFragment.ivMain.setImageResource(R.drawable.cloud_vector)
                }

                firstFragment.tvTemp.text =
                    (weatherList.main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
                firstFragment.tvPressure.text = weatherList.main.pressure.toString() + " hPa"

                secondFragment.tvWindStrength.text = weatherList.wind.speed.toString() + " m/s"
                secondFragment.tvWindDirection.text = "dir: " + degreesToDirection(weatherList.wind.deg)

                secondFragment.tvVisibility.text = (weatherList.visibility / 100).toString() + "%"
                secondFragment.tvHumidity.text = "RH: " + weatherList.main.humidity.toString() + "%"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupForecastUI() {
        val forecastResponseJsonString = mSharedPreferences.getString(forecastData, "")

        if (!forecastResponseJsonString.isNullOrEmpty()) {
            val weatherList = Gson().fromJson(forecastResponseJsonString, ForecastModel::class.java)
            thirdFragment.tvMain1.text = weatherList.list[0].weather[0].main
            thirdFragment.tvMainDesc1.text = (weatherList.list[0].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[0].weather[0].main) {
                "Clear" -> thirdFragment.ivMain1.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain1.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain1.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain1.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain1.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain1.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate = weatherList.list[0].dt_txt.split(" ")
            thirdFragment.tvDate1.text = splitDate[0]
            thirdFragment.tvHour1.text = splitDate[1]

            thirdFragment.tvMain2.text = weatherList.list[1].weather[0].main
            thirdFragment.tvMainDesc2.text = (weatherList.list[1].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[1].weather[0].main) {
                "Clear" -> thirdFragment.ivMain2.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain2.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain2.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain2.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain2.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain2.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate2 = weatherList.list[1].dt_txt.split(" ")
            thirdFragment.tvDate2.text = splitDate2[0]
            thirdFragment.tvHour2.text = splitDate2[1]

            thirdFragment.tvMain3.text = weatherList.list[2].weather[0].main
            thirdFragment.tvMainDesc3.text = (weatherList.list[2].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[2].weather[0].main) {
                "Clear" -> thirdFragment.ivMain3.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain3.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain3.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain3.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain3.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain3.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate3 = weatherList.list[2].dt_txt.split(" ")
            thirdFragment.tvDate3.text = splitDate3[0]
            thirdFragment.tvHour3.text = splitDate3[1]

            thirdFragment.tvMain4.text = weatherList.list[3].weather[0].main
            thirdFragment.tvMainDesc4.text = (weatherList.list[3].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[3].weather[0].main) {
                "Clear" -> thirdFragment.ivMain4.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain4.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain4.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain4.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain4.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain4.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate4 = weatherList.list[3].dt_txt.split(" ")
            thirdFragment.tvDate4.text = splitDate4[0]
            thirdFragment.tvHour4.text = splitDate4[1]

            thirdFragment.tvMain5.text = weatherList.list[4].weather[0].main
            thirdFragment.tvMainDesc5.text = (weatherList.list[4].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[4].weather[0].main) {
                "Clear" -> thirdFragment.ivMain5.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain5.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain5.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain5.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain5.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain5.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate5 = weatherList.list[4].dt_txt.split(" ")
            thirdFragment.tvDate5.text = splitDate5[0]
            thirdFragment.tvHour5.text = splitDate5[1]

            thirdFragment.tvMain6.text = weatherList.list[5].weather[0].main
            thirdFragment.tvMainDesc6.text = (weatherList.list[5].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[5].weather[0].main) {
                "Clear" -> thirdFragment.ivMain6.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain6.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain6.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain6.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain6.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain6.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate6 = weatherList.list[5].dt_txt.split(" ")
            thirdFragment.tvDate6.text = splitDate6[0]
            thirdFragment.tvHour6.text = splitDate6[1]

            thirdFragment.tvMain7.text = weatherList.list[6].weather[0].main
            thirdFragment.tvMainDesc7.text = (weatherList.list[6].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[6].weather[0].main) {
                "Clear" -> thirdFragment.ivMain7.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain7.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain7.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain7.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain7.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain7.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate7 = weatherList.list[6].dt_txt.split(" ")
            thirdFragment.tvDate7.text = splitDate7[0]
            thirdFragment.tvHour7.text = splitDate7[1]

            thirdFragment.tvMain8.text = weatherList.list[7].weather[0].main
            thirdFragment.tvMainDesc8.text = (weatherList.list[7].main.temp.toInt()).toString() + getUnit(application.resources.configuration.toString())
            when (weatherList.list[7].weather[0].main) {
                "Clear" -> thirdFragment.ivMain8.setImageResource(R.drawable.sun_vector)
                "Thunderstorm" -> thirdFragment.ivMain8.setImageResource(R.drawable.storm_vector)
                "Rain", "Drizzle" -> thirdFragment.ivMain8.setImageResource(R.drawable.water_vector)
                "Clouds" -> thirdFragment.ivMain8.setImageResource(R.drawable.cloud_vector)
                "Snow" -> thirdFragment.ivMain8.setImageResource(R.drawable.snow_vector)
                else -> thirdFragment.ivMain8.setImageResource(R.drawable.cloud_vector)
            }
            val splitDate8 = weatherList.list[7].dt_txt.split(" ")
            thirdFragment.tvDate8.text = splitDate8[0]
            thirdFragment.tvHour8.text = splitDate8[1]


        }
    }

    private fun getUnit(value: String): String {
        var unitValue = "°C"
        if (value == "US" || value == "LR" || value == "MM") {
            unitValue = "°F"
        }
        return unitValue
    }

    private fun degreesToDirection(degrees: Int): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        return directions[(degrees / 45 + 0.5).toInt() % 8]
    }
}
