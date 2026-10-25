package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment() {

    private lateinit var lvRecords: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lvRecords = view.findViewById(R.id.lvRecords)
        updateRecordsList()
    }

    override fun onResume() {
        super.onResume()
        updateRecordsList()
    }

    private fun updateRecordsList() {
        val db = AppDatabase.getDatabase(requireContext())
        val recordsList = db.recordDao().getAllRecordsDesc()
        val players = db.recordDao().getAllPlayers()

        val validRecords = recordsList.filter { it.score >= 0 }

        val displayStrings = validRecords.mapIndexed { index, record ->
            val player = players.find { it.id == record.playerId }
            val playerName = player?.fullName ?: "Игрок ${record.playerId}"
            "${index + 1}. $playerName — ${record.score} очков\nСложность: ${record.difficultyPreset} | ${record.dateString}"
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayStrings)
        lvRecords.adapter = adapter
    }
}