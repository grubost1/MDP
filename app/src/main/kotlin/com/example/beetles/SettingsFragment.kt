package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private val PREFS_NAME = "BeetlesGamePrefs"
    private val KEY_SPEED = "speed"
    private val KEY_MAX_INSECTS = "max_insects"
    private val KEY_DURATION = "duration"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSpeed = prefs.getInt(KEY_SPEED, 5)
        val savedMaxInsects = prefs.getInt(KEY_MAX_INSECTS, 20)
        val savedDuration = prefs.getInt(KEY_DURATION, 60)

        setupSeekBar(view, R.id.sbSpeed, R.id.tvSpeedValue, "x", 1, 10, savedSpeed, KEY_SPEED)
        setupSeekBar(view, R.id.sbMaxCockroaches, R.id.tvMaxCockroachesValue, " шт.", 5, 50, savedMaxInsects, KEY_MAX_INSECTS)
        setupSeekBar(view, R.id.sbBonusInterval, R.id.tvBonusIntervalValue, " сек.", 1, 10, 3, "bonus_interval") // Пока не используется в игре, но сохраняется
        setupSeekBar(view, R.id.sbRoundDuration, R.id.tvRoundDurationValue, " сек.", 30, 180, savedDuration, KEY_DURATION)
    }

    private fun setupSeekBar(view: View, seekBarId: Int, textViewId: Int, suffix: String, min: Int, max: Int, default: Int, key: String) {
        val seekBar = view.findViewById<SeekBar>(seekBarId)
        val textView = view.findViewById<TextView>(textViewId)

        seekBar.max = max - min
        seekBar.progress = default - min
        textView.text = "$default$suffix"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualValue = progress + min
                textView.text = "$actualValue$suffix"

                saveSetting(key, actualValue)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun saveSetting(key: String, value: Int) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(key, value).apply()
    }
}