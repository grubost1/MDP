package com.example.beetles

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvCurrentPlayerHeader: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCurrentPlayerHeader = view.findViewById(R.id.tvCurrentPlayerHeader)
        val btnStartGameSession = view.findViewById<Button>(R.id.btnStartGameSession)

        btnStartGameSession.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("BeetlesGamePrefs", Context.MODE_PRIVATE)
            val currentPlayerId = prefs.getInt("current_player_id", 0)

            if (currentPlayerId == 0) {
                Toast.makeText(requireContext(), "Сначала зарегистрируйтесь!", Toast.LENGTH_SHORT).show()
            } else {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main, GameFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("BeetlesGamePrefs", Context.MODE_PRIVATE)
        val currentPlayer = prefs.getString("current_player_name", "Не авторизован")
        val currentPlayerId = prefs.getInt("current_player_id", 0)

        if (currentPlayerId != 0) {
            tvCurrentPlayerHeader.text = "Текущий игрок: $currentPlayer (ID: $currentPlayerId)"
        } else {
            tvCurrentPlayerHeader.text = "Текущий игрок: Не авторизован"
        }
    }
}