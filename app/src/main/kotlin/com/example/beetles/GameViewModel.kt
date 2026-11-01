package com.example.beetles

import androidx.lifecycle.ViewModel
import java.util.UUID
import kotlin.random.Random

class GameViewModel : ViewModel() {
    var score: Int = 0
        private set
    var gameTime: Int = 60
        private set
    var isPlaying: Boolean = false
        private set
    var gameSpeed: Int = 10
    var maxInsectsLimit: Int = 20

    val activeBugsList = mutableListOf<Bug>()

    var isTiltModeActive = false
    var tiltX = 0f
    var tiltY = 0f

    fun addScore(points: Int) {
        score += points
    }

    fun decreaseTime() {
        if (gameTime > 0) gameTime--
    }

    fun setGameTime(time: Int) {
        this.gameTime = time
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
    }

    fun reset() {
        score = 0
        activeBugsList.clear()
        isTiltModeActive = false
        tiltX = 0f
        tiltY = 0f
    }

    fun spawnLogicalBug() {
        if (activeBugsList.size >= maxInsectsLimit) return
        val availableResources = listOf(
            R.drawable.bug_green,
            R.drawable.bug_red,
            R.drawable.bug_blue,
            R.drawable.bug_dr
        )
        activeBugsList.add(Bug.createRandomBug(availableResources))
    }

    fun spawnGoldenBug() {
        if (activeBugsList.size >= maxInsectsLimit) return
        val bugEntity = Bug(
            id = UUID.randomUUID().toString(),
            logicalX = Random.nextFloat() * 0.8f,
            logicalY = Random.nextFloat() * 0.8f,
            speed = 1.4f,
            logicalSize = 0.10f,
            type = BugType.GOLD,
            visualResource = R.drawable.bug_gold
        )
        activeBugsList.add(bugEntity)
    }

    fun removeBug(bug: Bug) {
        activeBugsList.remove(bug)
    }
}
