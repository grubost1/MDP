package com.example.beetles

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStartGameSession = view.findViewById<Button>(R.id.btnStartGameSession)

        btnStartGameSession.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main, GameFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
