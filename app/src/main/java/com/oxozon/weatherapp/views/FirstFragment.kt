package com.oxozon.weatherapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.oxozon.weatherapp.R

class FirstFragment : Fragment(R.layout.first_fragment) {
    lateinit var tvCity: TextView
    lateinit var tvCountry: TextView
    lateinit var tvLatitude: TextView
    lateinit var tvLongitude: TextView
    lateinit var ivMain: ImageView
    lateinit var tvMain: TextView
    lateinit var tvMainDescription : TextView
    lateinit var tvTemp: TextView
    lateinit var tvPressure: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.first_fragment, container, false)
        tvCity = view.findViewById(R.id.tv_city)
        tvCountry = view.findViewById(R.id.tv_country)
        tvLatitude = view.findViewById(R.id.tv_latitude)
        tvLongitude = view.findViewById(R.id.tv_longitude)
        ivMain = view.findViewById(R.id.iv_main)
        tvMain = view.findViewById(R.id.tv_main)
        tvMainDescription= view.findViewById(R.id.tv_main_description)
        tvTemp = view.findViewById(R.id.tv_temp)
        tvPressure = view.findViewById(R.id.tv_pressure)
        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).onFirstFragmentCreated()
    }
}