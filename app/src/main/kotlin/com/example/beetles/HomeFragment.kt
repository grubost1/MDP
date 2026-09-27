package com.example.beetles

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var gameContainer: RelativeLayout
    private lateinit var scoreText: TextView
    private lateinit var timeText: TextView
    private lateinit var btnStart: FloatingActionButton

    private val handler = Handler(Looper.getMainLooper())
    private var score = 0

    private var gameTime = 60
    private var maxInsectsLimit = 20
    private var gameSpeed = 5

    private var isPlaying = false
    private val insects = mutableListOf<TextView>()

    private var spawnRunnable: Runnable? = null
    private var moveRunnable: Runnable? = null
    private var timerRunnable: Runnable? = null

    private val PREFS_NAME = "BeetlesGamePrefs"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        loadSettings()
    }

    private fun initViews(view: View) {
        gameContainer = view.findViewById(R.id.gameContainer)
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
        gameContainer.setOnClickListener {
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
        btnStart.setImageResource(R.drawable.ic_stop)

        gameContainer.removeAllViews()
        gameContainer.addView(scoreText)
        gameContainer.addView(timeText)
        gameContainer.addView(btnStart)
        insects.clear()

        startSpawning()
        startMoving()
        startTimer()
    }

    private fun stopGame() {
        isPlaying = false
        btnStart.setImageResource(R.drawable.ic_play)

        spawnRunnable?.let { handler.removeCallbacks(it) }
        moveRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable?.let { handler.removeCallbacks(it) }

        insects.toList().forEach { removeInsect(it, animate = true) }

        Toast.makeText(requireContext(), "Игра окончена! Ваш счет: $score", Toast.LENGTH_LONG).show()
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
                    val currentMoveInterval = (1000 - (gameSpeed * 99)).toLong().coerceAtLeast(10L)
                    handler.postDelayed(this, currentMoveInterval)
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

        val insect = TextView(requireContext()).apply {
            text = listOf("🐞", "🦋", "🐜", "🕷️", "🪲").random()
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val size = 100
        val layoutParams = RelativeLayout.LayoutParams(size, size).apply {
            val maxX = (gameContainer.width - size).coerceAtLeast(0)
            val maxY = (gameContainer.height - 120).coerceAtLeast(0)
            leftMargin = Random.nextInt(0, maxX)
            topMargin = Random.nextInt(0, maxY)
        }

        insect.layoutParams = layoutParams

        insect.setOnClickListener {
            if (isPlaying) {
                score += 10
                updateUI()
                removeInsect(insect, animate = true)
            }
        }

        gameContainer.addView(insect)
        insects.add(insect)

        insect.alpha = 0f
        insect.scaleX = 0f
        insect.scaleY = 0f
        insect.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start()
    }

    private fun moveInsects() {
        insects.forEach { insect ->
            val lp = insect.layoutParams as RelativeLayout.LayoutParams

            val jumpRange = 40 * gameSpeed

            var newLeft = lp.leftMargin + Random.nextInt(-jumpRange, jumpRange + 1)
            var newTop = lp.topMargin + Random.nextInt(-jumpRange, jumpRange + 1)

            val maxX = (gameContainer.width - insect.width).coerceAtLeast(0)
            val maxY = (gameContainer.height - 120).coerceAtLeast(0)

            lp.leftMargin = newLeft.coerceIn(0, maxX)
            lp.topMargin = newTop.coerceIn(0, maxY)
            insect.layoutParams = lp
        }
    }

    private fun removeInsect(insect: TextView, animate: Boolean) {
        insects.remove(insect)
        if (animate) {
            insect.animate()
                .alpha(0f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        gameContainer.removeView(insect)
                    }
                })
                .start()
        } else {
            gameContainer.removeView(insect)
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