package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStartGameSession = view.findViewById<Button>(R.id.btnStartGameSession)
        val tvCurrentPlayerHeader = view.findViewById<TextView>(R.id.tvCurrentPlayerHeader)

        val prefs = requireContext().getSharedPreferences("BeetlesGamePrefs", Context.MODE_PRIVATE)
        val currentPlayer = prefs.getString("current_player_name", "Не авторизован")
        tvCurrentPlayerHeader.text = "Текущий игрок: $currentPlayer"

        btnStartGameSession.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main, GameFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
