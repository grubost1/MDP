package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lvRecords = view.findViewById<ListView>(R.id.lvRecords)

        val recordsList = AppDatabase.getDatabase(requireContext()).recordDao().getAllRecordsDesc()

        val displayStrings = recordsList.mapIndexed { index, record ->
            "${index + 1}. ${record.fullName} — ${record.maxScore} очков\nСложность: ${record.difficultyPreset} | ${record.dateString}"
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, displayStrings)
        lvRecords.adapter = adapter
    }
}
