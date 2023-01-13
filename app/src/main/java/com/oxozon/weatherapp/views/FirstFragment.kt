package com.oxozon.weatherapp.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.oxozon.weatherapp.R
import com.oxozon.weatherapp.models.Main

class FirstFragment : Fragment(R.layout.first_fragment) {
     var tvMain: TextView? = null
     var tvMainDescription : TextView? = null
     var tvTemp: TextView? = null
     var tvHumidity: TextView? = null
     var tvMin: TextView? = null
     var tvMax: TextView? = null
     var tvSpeed: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.first_fragment, container, false)
        Log.d("test", "creating fragment!")
        Log.d("test", "setting variables!")
        tvMain = view.findViewById(R.id.tv_main)
        tvMainDescription= view.findViewById(R.id.tv_main_description)
        tvTemp = view.findViewById(R.id.tv_temp)
        tvHumidity = view.findViewById(R.id.tv_humidity)
        tvMin = view.findViewById(R.id.tv_min)
        tvMax = view.findViewById(R.id.tv_max)
        tvSpeed = view.findViewById(R.id.tv_speed)
        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).onFirstFragmentCreated()
    }
}