package com.oxozon.weatherapp.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.oxozon.weatherapp.R

class SecondFragment : Fragment(R.layout.second_fragment) {
    lateinit var tvWindStrength: TextView
    lateinit var tvWindDirection: TextView
    lateinit var tvVisibility: TextView
    lateinit var tvHumidity: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.second_fragment, container, false)
        tvWindStrength = view.findViewById(R.id.tv_wind_strength)
        tvWindDirection = view.findViewById(R.id.tv_wind_direction)
        tvVisibility = view.findViewById(R.id.tv_visibility)
        tvHumidity = view.findViewById(R.id.tv_humidity)
        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).onSecondFragmentCreated()
    }
}