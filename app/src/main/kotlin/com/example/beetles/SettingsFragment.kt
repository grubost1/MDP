package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    private val KEY_BONUS_INTERVAL = "bonus_interval"
    private lateinit var sbBonusInterval: SeekBar
    private lateinit var tvBonusInterval: TextView

    private val PREFS_NAME = "BeetlesGamePrefs"
    private val KEY_SPEED = "speed"
    private val KEY_MAX_INSECTS = "max_insects"
    private val KEY_DURATION = "duration"
    private val KEY_PRESET = "difficulty_preset"

    private lateinit var sbSpeed: SeekBar
    private lateinit var sbMaxInsects: SeekBar
    private lateinit var sbDuration: SeekBar

    private lateinit var tvSpeed: TextView
    private lateinit var tvMaxInsects: TextView
    private lateinit var tvDuration: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSpeed = prefs.getInt(KEY_SPEED, 5)
        val savedMaxInsects = prefs.getInt(KEY_MAX_INSECTS, 20)
        val savedDuration = prefs.getInt(KEY_DURATION, 60)
        val savedPreset = prefs.getString(KEY_PRESET, "Средний")

        val rgPreset = view.findViewById<RadioGroup>(R.id.rgDifficultyPreset)
        sbSpeed = view.findViewById(R.id.sbSpeed)
        sbMaxInsects = view.findViewById(R.id.sbMaxCockroaches)
        sbDuration = view.findViewById(R.id.sbRoundDuration)

        tvSpeed = view.findViewById(R.id.tvSpeedValue)
        tvMaxInsects = view.findViewById(R.id.tvMaxCockroachesValue)
        tvDuration = view.findViewById(R.id.tvRoundDurationValue)

        setupSeekBar(sbSpeed, tvSpeed, "x", 1, 10, savedSpeed, KEY_SPEED)
        setupSeekBar(sbMaxInsects, tvMaxInsects, " шт.", 5, 50, savedMaxInsects, KEY_MAX_INSECTS)
        setupSeekBar(sbDuration, tvDuration, " сек.", 30, 180, savedDuration, KEY_DURATION)

        sbBonusInterval = view.findViewById(R.id.sbBonusInterval)
        tvBonusInterval = view.findViewById(R.id.tvBonusIntervalValue)
        val savedBonusInterval = prefs.getInt(KEY_BONUS_INTERVAL, 5)
        sbBonusInterval.max = 19
        sbBonusInterval.progress = savedBonusInterval - 1
        tvBonusInterval.text = "$savedBonusInterval сек."

        sbBonusInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val interval = progress + 1
                tvBonusInterval.text = "$interval сек."
                prefs.edit().putInt(KEY_BONUS_INTERVAL, interval).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        when (savedPreset) {
            "Легкий" -> view.findViewById<RadioButton>(R.id.rbEasy).isChecked = true
            "Сложный" -> view.findViewById<RadioButton>(R.id.rbHard).isChecked = true
            else -> view.findViewById<RadioButton>(R.id.rbMedium).isChecked = true
        }

        rgPreset.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.rbEasy -> {
                    updateSlidersAndSave(1, 10, 45)
                    editor.putString(KEY_PRESET, "Легкий")
                }
                R.id.rbHard -> {
                    updateSlidersAndSave(8, 35, 90)
                    editor.putString(KEY_PRESET, "Сложный")
                }
                R.id.rbMedium -> {
                    updateSlidersAndSave(5, 20, 60)
                    editor.putString(KEY_PRESET, "Средний")
                }
            }
            editor.apply()
        }
    }

    private fun updateSlidersAndSave(speed: Int, maxInsects: Int, duration: Int) {
        sbSpeed.progress = speed - 1
        tvSpeed.text = "${speed}x"
        saveSetting(KEY_SPEED, speed)

        sbMaxInsects.progress = maxInsects - 5
        tvMaxInsects.text = "$maxInsects шт."
        saveSetting(KEY_MAX_INSECTS, maxInsects)

        sbDuration.progress = duration - 30
        tvDuration.text = "$duration сек."
        saveSetting(KEY_DURATION, duration)
    }

    private fun setupSeekBar(seekBar: SeekBar, textView: TextView, suffix: String, min: Int, max: Int, default: Int, key: String) {
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
