package com.shigaga.restfuldemo.ui.second_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shigaga.restfuldemo.R
import kotlinx.android.synthetic.main.second_fragment.*

class SecondFragment : Fragment() {

    private var startTimeText: String? = null
    private var endTimeText: String? = null
    private var temperatureText: String? = null

    companion object {
        private const val ARG_START_TIME = "startTime"
        private const val ARG_END_TIME = "endTime"
        private const val ARG_TEMPERATURE = "temperature"

        @JvmStatic
        fun newInstance(startTime: String, endTime: String, temperature: String) =
            SecondFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_START_TIME, startTime)
                    putString(ARG_END_TIME, endTime)
                    putString(ARG_TEMPERATURE, temperature)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            startTimeText = it.getString(ARG_START_TIME)
            endTimeText = it.getString(ARG_END_TIME)
            temperatureText = it.getString(ARG_TEMPERATURE)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.second_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startTime2.text = startTimeText
        endTime2.text = endTimeText
        temperature2.text = temperatureText
    }
}
