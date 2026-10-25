package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFragment : Fragment() {

    private lateinit var etFullName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var spinnerCourse: Spinner
    private lateinit var calendarView: CalendarView
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView
    private lateinit var ivZodiac: ImageView

    private var selectedDate: Date = Date()
    private val PREFS_NAME = "BeetlesGamePrefs"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etFullName = view.findViewById(R.id.etFullName)
        rgGender = view.findViewById(R.id.rgGender)
        spinnerCourse = view.findViewById(R.id.spinnerCourse)
        calendarView = view.findViewById(R.id.calendarView)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        tvResult = view.findViewById(R.id.tvResult)
        ivZodiac = view.findViewById(R.id.ivZodiac)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "Магистратура")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        etFullName.setText(prefs.getString("current_player_name", ""))

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        }

        btnSubmit.setOnClickListener {
            val name = etFullName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Введите ФИО!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedGenderId = rgGender.checkedRadioButtonId
            val gender = if (selectedGenderId == R.id.rbMale) "Мужской" else "Женский"
            val course = spinnerCourse.selectedItem.toString()
            val zodiacSign = getZodiacSign(selectedDate)
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(selectedDate)

            // Сохраняем ТЕКУЩЕГО активного игрока глобально
            prefs.edit().apply {
                putString("current_player_name", name)
                putString("current_player_gender", gender)
                putString("current_player_course", course)
                putString("current_player_birthday", dateStr)
                putString("current_player_zodiac", zodiacSign)
                apply()
            }

            tvResult.text = "Игрок $name успешно авторизован для игры!"
            ivZodiac.setImageResource(getZodiacImageResource(zodiacSign))
        }
    }

    private fun getZodiacSign(date: Date): String {
        val calendar = Calendar.getInstance().apply { time = date }
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> "Овен"
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> "Телец"
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> "Близнецы"
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> "Рак"
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> "Лев"
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> "Дева"
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> "Весы"
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> "Скорпион"
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> "Стрелец"
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> "Козерог"
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "Водолей"
            else -> "Рыбы"
        }
    }

    private fun getZodiacImageResource(zodiacSign: String): Int {
        return when (zodiacSign) {
            "Овен" -> R.drawable.aries
            "Телец" -> R.drawable.taurus
            "Близнецы" -> R.drawable.gemini
            "Рак" -> R.drawable.cancer
            "Лев" -> R.drawable.leo
            "Дева" -> R.drawable.virgo
            "Весы" -> R.drawable.libra
            "Скорпион" -> R.drawable.scorpio
            "Стрелец" -> R.drawable.sagittarius
            "Козерог" -> R.drawable.capricorn
            "Водолей" -> R.drawable.aquarius
            "Рыбы" -> R.drawable.pisces
            else -> R.drawable.ic_launcher_foreground
        }
    }
}
