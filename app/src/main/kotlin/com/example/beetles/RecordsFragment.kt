package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment() {

    private lateinit var lvRecords: ListView
    private lateinit var spinnerFilterDifficulty: Spinner
    private var selectedFilter: String = "Все"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lvRecords = view.findViewById(R.id.lvRecords)
        spinnerFilterDifficulty = view.findViewById(R.id.spinnerFilterDifficulty)

        val filters = arrayOf("Все", "Легкий", "Средний", "Сложный")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterDifficulty.adapter = filterAdapter

        spinnerFilterDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFilter = filters[position]
                updateRecordsList()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateRecordsList()
    }

    override fun onResume() {
        super.onResume()
        updateRecordsList()
    }

    private fun getDifficultyWeight(preset: String): Int {
        return when (preset) {
            "Сложный" -> 3
            "Средний" -> 2
            "Легкий" -> 1
            else -> 0
        }
    }

    private fun updateRecordsList() {
        val db = AppDatabase.getDatabase(requireContext())
        val recordsList = db.recordDao().getAllRecordsDesc()
        val players = db.recordDao().getAllPlayers()

        val filteredRecords = recordsList.filter { record ->
            val isScoreValid = record.score >= 0
            val matchesFilter = selectedFilter == "Все" || record.difficultyPreset == selectedFilter
            isScoreValid && matchesFilter
        }

        val sortedRecords = filteredRecords.sortedWith(
            compareByDescending<PlayerRecord> { it.score }
                .thenByDescending { getDifficultyWeight(it.difficultyPreset) }
        )

        val displayStrings = sortedRecords.mapIndexed { index, record ->
            val player = players.find { it.id == record.playerId }
            val playerName = player?.fullName ?: "Игрок ${record.playerId}"
            "${index + 1}. $playerName — ${record.score} очков\nСложность: ${record.difficultyPreset} | ${record.dateString}"
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayStrings)
        lvRecords.adapter = adapter
    }
}
