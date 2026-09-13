package com.example.beetles

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import java.util.Date

data class Player(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacSign: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var spinnerCourse: Spinner
    private lateinit var seekBarDifficulty: SeekBar
    private lateinit var calendarView: CalendarView
    private lateinit var btnSubmit: Button
    private lateinit var tvResult: TextView
    private lateinit var ivZodiac: ImageView

    private var selectedDate: Date = Date()
    private lateinit var tvDifficultyValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etFullName = findViewById(R.id.etFullName)
        rgGender = findViewById(R.id.rgGender)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        seekBarDifficulty = findViewById(R.id.seekBarDifficulty)
        calendarView = findViewById(R.id.calendarView)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvResult = findViewById(R.id.tvResult)
        ivZodiac = findViewById(R.id.ivZodiac)

        tvDifficultyValue = findViewById(R.id.tvDifficultyValue)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "Магистратура")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter

        seekBarDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDifficultyValue.text = "Уровень сложности: $progress/10"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        }

        btnSubmit.setOnClickListener {
            val player = collectData()
            displayData(player)
        }
    }

    private fun collectData(): Player {
        val fullName = etFullName.text.toString()

        val selectedGenderId = rgGender.checkedRadioButtonId
        val gender = if (selectedGenderId == R.id.rbMale) "Мужской" else "Женский"

        val course = spinnerCourse.selectedItem.toString()
        val difficulty = seekBarDifficulty.progress

        val zodiacSign = getZodiacSign(selectedDate)
        val dateString = "${selectedDate.date}.${selectedDate.month + 1}.${selectedDate.year + 1900}"

        return Player(fullName, gender, course, difficulty, dateString, zodiacSign)
    }

    private fun displayData(player: Player) {
        val resultText = """
            ФИО: ${player.fullName}
            Пол: ${player.gender}
            Курс: ${player.course}
            Уровень сложности: ${player.difficulty}/10
            Дата рождения: ${player.birthDate}
            Знак зодиака: ${player.zodiacSign}
        """.trimIndent()

        tvResult.text = resultText

        val zodiacImageRes = getZodiacImageResource(player.zodiacSign)
        ivZodiac.setImageResource(zodiacImageRes)
    }

    private fun getZodiacSign(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
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
