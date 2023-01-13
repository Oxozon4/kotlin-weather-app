package com.oxozon.weatherapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.oxozon.weatherapp.R

class ThirdFragment : Fragment() {
    lateinit var tvMain1: TextView
    lateinit var tvMainDesc1: TextView
    lateinit var ivMain1: ImageView
    lateinit var tvDate1: TextView
    lateinit var tvHour1: TextView

    lateinit var tvMain2: TextView
    lateinit var tvMainDesc2: TextView
    lateinit var ivMain2: ImageView
    lateinit var tvDate2: TextView
    lateinit var tvHour2: TextView

    lateinit var tvMain3: TextView
    lateinit var tvMainDesc3: TextView
    lateinit var ivMain3: ImageView
    lateinit var tvDate3: TextView
    lateinit var tvHour3: TextView

    lateinit var tvMain4: TextView
    lateinit var tvMainDesc4: TextView
    lateinit var ivMain4: ImageView
    lateinit var tvDate4: TextView
    lateinit var tvHour4: TextView

    lateinit var tvMain5: TextView
    lateinit var tvMainDesc5: TextView
    lateinit var ivMain5: ImageView
    lateinit var tvDate5: TextView
    lateinit var tvHour5: TextView

    lateinit var tvMain6: TextView
    lateinit var tvMainDesc6: TextView
    lateinit var ivMain6: ImageView
    lateinit var tvDate6: TextView
    lateinit var tvHour6: TextView

    lateinit var tvMain7: TextView
    lateinit var tvMainDesc7: TextView
    lateinit var ivMain7: ImageView
    lateinit var tvDate7: TextView
    lateinit var tvHour7: TextView

    lateinit var tvMain8: TextView
    lateinit var tvMainDesc8: TextView
    lateinit var ivMain8: ImageView
    lateinit var tvDate8: TextView
    lateinit var tvHour8: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.third_fragment, container, false)
        tvMain1 = view.findViewById(R.id.tv_main)
        tvMainDesc1 = view.findViewById(R.id.tv_main_description)
        ivMain1 = view.findViewById(R.id.iv_main)
        tvDate1 = view.findViewById(R.id.tv_date)
        tvHour1 = view.findViewById(R.id.tv_hour)

        tvMain2 = view.findViewById(R.id.tv_main2)
        tvMainDesc2 = view.findViewById(R.id.tv_main_description2)
        ivMain2 = view.findViewById(R.id.iv_main2)
        tvDate2 = view.findViewById(R.id.tv_date2)
        tvHour2 = view.findViewById(R.id.tv_hour2)

        tvMain3 = view.findViewById(R.id.tv_main3)
        tvMainDesc3 = view.findViewById(R.id.tv_main_description3)
        ivMain3 = view.findViewById(R.id.iv_main3)
        tvDate3 = view.findViewById(R.id.tv_date3)
        tvHour3 = view.findViewById(R.id.tv_hour3)

        tvMain4 = view.findViewById(R.id.tv_main4)
        tvMainDesc4 = view.findViewById(R.id.tv_main_description4)
        ivMain4 = view.findViewById(R.id.iv_main4)
        tvDate4 = view.findViewById(R.id.tv_date4)
        tvHour4 = view.findViewById(R.id.tv_hour4)

        tvMain5 = view.findViewById(R.id.tv_main5)
        tvMainDesc5 = view.findViewById(R.id.tv_main_description5)
        ivMain5 = view.findViewById(R.id.iv_main5)
        tvDate5 = view.findViewById(R.id.tv_date5)
        tvHour5 = view.findViewById(R.id.tv_hour5)

        tvMain6 = view.findViewById(R.id.tv_main6)
        tvMainDesc6 = view.findViewById(R.id.tv_main_description6)
        ivMain6 = view.findViewById(R.id.iv_main6)
        tvDate6 = view.findViewById(R.id.tv_date6)
        tvHour6 = view.findViewById(R.id.tv_hour6)

        tvMain7 = view.findViewById(R.id.tv_main7)
        tvMainDesc7 = view.findViewById(R.id.tv_main_description7)
        ivMain7 = view.findViewById(R.id.iv_main7)
        tvDate7 = view.findViewById(R.id.tv_date7)
        tvHour7 = view.findViewById(R.id.tv_hour7)

        tvMain8 = view.findViewById(R.id.tv_main8)
        tvMainDesc8 = view.findViewById(R.id.tv_main_description8)
        ivMain8 = view.findViewById(R.id.iv_main8)
        tvDate8 = view.findViewById(R.id.tv_date8)
        tvHour8 = view.findViewById(R.id.tv_hour8)

        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).onThirdFragmentCreated()
    }
}