package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSeekBar(view, R.id.sbSpeed, R.id.tvSpeedValue, "x", 1, 10, 5)
        setupSeekBar(view, R.id.sbMaxCockroaches, R.id.tvMaxCockroachesValue, " шт.", 5, 50, 20)
        setupSeekBar(view, R.id.sbBonusInterval, R.id.tvBonusIntervalValue, " сек.", 1, 10, 3)
        setupSeekBar(view, R.id.sbRoundDuration, R.id.tvRoundDurationValue, " сек.", 30, 180, 60)
    }

    private fun setupSeekBar(view: View, seekBarId: Int, textViewId: Int, suffix: String, min: Int, max: Int, default: Int) {
        val seekBar = view.findViewById<SeekBar>(seekBarId)
        val textView = view.findViewById<TextView>(textViewId)

        seekBar.max = max - min
        seekBar.progress = default - min

        textView.text = "$default$suffix"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualValue = progress + min
                textView.text = "$actualValue$suffix"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}