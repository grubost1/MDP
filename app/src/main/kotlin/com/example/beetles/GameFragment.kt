package com.example.beetles

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random

class GameFragment : Fragment(R.layout.fragment_game) {

    private lateinit var gameContainer: RelativeLayout
    private lateinit var bugSpawnArea: RelativeLayout
    private lateinit var scoreText: TextView
    private lateinit var timeText: TextView
    private lateinit var btnStart: FloatingActionButton

    private val handler = Handler(Looper.getMainLooper())
    private var score = 0

    private var gameTime = 60
    private var maxInsectsLimit = 20
    private var gameSpeed = 5

    private var isPlaying = false
    private val insects = mutableListOf<ImageView>()

    private var spawnRunnable: Runnable? = null
    private var moveRunnable: Runnable? = null
    private var timerRunnable: Runnable? = null

    private val PREFS_NAME = "BeetlesGamePrefs"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadSettings()

        bugSpawnArea.post {
            startGame()
        }
    }

    private fun initViews(view: View) {
        gameContainer = view.findViewById(R.id.gameContainer)
        bugSpawnArea = view.findViewById(R.id.bugSpawnArea)
        scoreText = view.findViewById(R.id.scoreText)
        timeText = view.findViewById(R.id.timeText)
        btnStart = view.findViewById(R.id.btnStart)
        updateUI()
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gameSpeed = prefs.getInt("speed", 5)
        maxInsectsLimit = prefs.getInt("max_insects", 20)
        gameTime = prefs.getInt("duration", 60)
    }

    private fun setupListeners() {
        bugSpawnArea.setOnClickListener {
            if (isPlaying) {
                score -= 5
                updateUI()
                Toast.makeText(requireContext(), "Промах! -5 очков", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            if (isPlaying) {
                stopGame()
            } else {
                startGame()
            }
        }
    }

    private fun startGame() {
        isPlaying = true
        score = 0

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        gameSpeed = prefs.getInt("speed", 5)
        gameTime = prefs.getInt("duration", 60)
        maxInsectsLimit = prefs.getInt("max_insects", 20)

        updateUI()

        btnStart.setImageResource(android.R.drawable.ic_media_pause)

        bugSpawnArea.removeAllViews()
        insects.clear()

        startSpawning()
        startMoving()
        startTimer()
    }

    private fun stopGame() {
        isPlaying = false
        btnStart.setImageResource(android.R.drawable.ic_media_play)

        spawnRunnable?.let { handler.removeCallbacks(it) }
        moveRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable?.let { handler.removeCallbacks(it) }

        insects.toList().forEach { removeInsect(it, animate = true) }

        Toast.makeText(requireContext(), "Игра окончена! Ваш счет: $score", Toast.LENGTH_LONG).show()

        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun startSpawning() {
        spawnRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    spawnInsect()
                    val currentSpawnInterval = (1500 - (gameSpeed * 140)).toLong().coerceAtLeast(100L)
                    handler.postDelayed(this, currentSpawnInterval)
                }
            }
        }
        handler.post(spawnRunnable!!)
    }

    private fun startMoving() {
        moveRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    moveInsects()
                    handler.postDelayed(this, 450L)
                }
            }
        }
        handler.post(moveRunnable!!)
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    gameTime--
                    updateUI()
                    if (gameTime <= 0) {
                        stopGame()
                    } else {
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun spawnInsect() {
        if (insects.size >= maxInsectsLimit) return

        val insect = ImageView(requireContext()).apply {
            val randomBugRes = listOf(R.drawable.bug_green, R.drawable.bug_red, R.drawable.bug_blue, R.drawable.bug_dr).random()
            setImageResource(randomBugRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val randomSize = 160
        insect.tag = randomSize

        val layoutParams = RelativeLayout.LayoutParams(randomSize, randomSize)
        insect.layoutParams = layoutParams

        val maxX = (bugSpawnArea.width - randomSize).coerceAtLeast(0)
        val maxY = (bugSpawnArea.height - randomSize).coerceAtLeast(0)
        val startX = Random.nextInt(0, maxX).toFloat()
        val startY = Random.nextInt(0, maxY).toFloat()

        insect.translationX = startX
        insect.translationY = startY

        insect.setOnClickListener {
            if (isPlaying) {
                score += 10
                updateUI()
                removeInsect(insect, animate = true)
            }
        }

        bugSpawnArea.addView(insect)
        insects.add(insect)

        insect.alpha = 0f
        insect.scaleX = 0f
        insect.scaleY = 0f
        insect.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()
    }

    private fun moveInsects() {
        if (!isPlaying) return

        insects.forEach { insect ->
            val jumpRange = 50f * gameSpeed
            val insectSize = insect.tag as Int

            val deltaX = Random.nextFloat() * (jumpRange * 2) - jumpRange
            val deltaY = Random.nextFloat() * (jumpRange * 2) - jumpRange

            val maxX = (bugSpawnArea.width - insectSize).toFloat().coerceAtLeast(0f)
            val maxY = (bugSpawnArea.height - insectSize).toFloat().coerceAtLeast(0f)

            val targetX = (insect.translationX + deltaX).coerceIn(0f, maxX)
            val targetY = (insect.translationY + deltaY).coerceIn(0f, maxY)

            val moveVectorX = (targetX - insect.translationX).toDouble()
            val moveVectorY = (targetY - insect.translationY).toDouble()

            if (moveVectorX != 0.0 || moveVectorY != 0.0) {
                var angleDegrees = Math.toDegrees(Math.atan2(moveVectorY, moveVectorX))
                angleDegrees += 90.0
                insect.rotation = angleDegrees.toFloat()
            }

            insect.animate()
                .translationX(targetX)
                .translationY(targetY)
                .setDuration(400)
                .setInterpolator(android.view.animation.LinearInterpolator())
                .start()
        }
    }

    private fun removeInsect(insect: ImageView, animate: Boolean) {
        insects.remove(insect)
        if (animate) {
            insect.animate()
                .alpha(0f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        bugSpawnArea.removeView(insect)
                    }
                })
                .start()
        } else {
            bugSpawnArea.removeView(insect)
        }
    }

    private fun updateUI() {
        scoreText.text = "Очки: $score"
        val mins = gameTime / 60
        val secs = gameTime % 60
        timeText.text = "Время: ${mins}:${secs.toString().padStart(2, '0')}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopGame()
    }
}
