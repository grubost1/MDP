package com.example.beetles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)
        val listView = view.findViewById<ListView>(R.id.lvAuthors)

        val authorsList = listOf(
            Author("Монастырный Михаил Евгеньевич", R.drawable.mish),
            Author("Шпаков Данил Сергеевич", R.drawable.dan)
        )

        listView.adapter = AuthorsAdapter(requireContext(), authorsList)
        return view
    }
}