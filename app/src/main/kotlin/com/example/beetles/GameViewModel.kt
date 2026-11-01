package com.example.beetles

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var score: Int = 0
        private set
    var gameTime: Int = 60
        private set
    var isPlaying: Boolean = false
        private set

    fun addScore(points: Int) {
        score += points
    }

    fun decreaseTime() {
        if (gameTime > 0) gameTime--
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
    }

    fun reset() {
        score = 0
        gameTime = 60
        isPlaying = false
    }
}