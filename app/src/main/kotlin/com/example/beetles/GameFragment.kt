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
    private var gameSpeed = 10
    private var isPlaying = false

    private val bugViewsMap = mutableMapOf<String, ImageView>()
    private val activeBugsList = mutableListOf<Bug>()

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
            if (isPlaying) stopGame() else startGame()
        }
    }

    private fun startGame() {
        isPlaying = true
        score = 0
        loadSettings()
        updateUI()

        btnStart.setImageResource(android.R.drawable.ic_media_pause)
        bugSpawnArea.removeAllViews()
        bugViewsMap.clear()
        activeBugsList.clear()

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

        activeBugsList.toList().forEach { removeBugFromSandbox(it, animate = true) }
        Toast.makeText(requireContext(), "Игра окончена! Ваш счет: $score", Toast.LENGTH_LONG).show()

        val prefs = requireContext().getSharedPreferences("BeetlesGamePrefs", Context.MODE_PRIVATE)
        val name = prefs.getString("current_player_name", "Аноним") ?: "Аноним"
        val gender = prefs.getString("current_player_gender", "—") ?: "—"
        val course = prefs.getString("current_player_course", "—") ?: "—"
        val preset = prefs.getString("difficulty_preset", "Средний") ?: "Средний"

        val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

        val newRecord = PlayerRecord(
            fullName = name, gender = gender, course = course, difficultyPreset = preset, maxScore = score, dateString = dateStr
        )
        AppDatabase.getDatabase(requireContext()).recordDao().insertRecord(newRecord)

        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun startSpawning() {
        spawnRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    spawnLogicalBug()
                    val interval = (1500 - (gameSpeed * 140)).toLong().coerceAtLeast(100L)
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(spawnRunnable!!)
    }

    private fun startMoving() {
        moveRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    moveLogicalBugsSmoothly()
                    handler.postDelayed(this, 330L)
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
                    if (gameTime <= 0) stopGame() else handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun spawnLogicalBug() {
        if (activeBugsList.size >= maxInsectsLimit) return

        val availableResources = listOf(
            R.drawable.bug_green,
            R.drawable.bug_red,
            R.drawable.bug_blue,
            R.drawable.bug_dr
        )

        val bugEntity = Bug.createRandomBug(availableResources)
        activeBugsList.add(bugEntity)

        val viewSizePx = (bugSpawnArea.width * bugEntity.logicalSize).toInt()

        val bugView = ImageView(requireContext()).apply {
            setImageResource(bugEntity.visualResource)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = RelativeLayout.LayoutParams(viewSizePx, viewSizePx)

            translationX = bugEntity.logicalX * bugSpawnArea.width
            translationY = bugEntity.logicalY * bugSpawnArea.height
        }

        bugView.setOnClickListener {
            if (isPlaying) {
                score += bugEntity.type.scoreValue
                updateUI()
                removeBugFromSandbox(bugEntity, animate = true)
            }
        }

        bugViewsMap[bugEntity.id] = bugView
        bugSpawnArea.addView(bugView)

        bugView.alpha = 0f
        bugView.scaleX = 0f
        bugView.scaleY = 0f
        bugView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()
    }

    private fun moveLogicalBugsSmoothly() {
        activeBugsList.forEach { bugEntity ->
            val bugView = bugViewsMap[bugEntity.id] ?: return@forEach

            val oldPxX = bugView.translationX
            val oldPxY = bugView.translationY

            bugEntity.calculateNextLogicalPosition(gameSpeed)

            val targetPxX = bugEntity.logicalX * bugSpawnArea.width
            val targetPxY = bugEntity.logicalY * bugSpawnArea.height

            val deltaPxX = (targetPxX - oldPxX).toDouble()
            val deltaPxY = (targetPxY - oldPxY).toDouble()

            if (deltaPxX != 0.0 || deltaPxY != 0.0) {
                var angle = Math.toDegrees(Math.atan2(deltaPxY, deltaPxX))
                angle += 90.0
                bugView.rotation = angle.toFloat()
            }

            bugView.animate()
                .translationX(targetPxX)
                .translationY(targetPxY)
                .setDuration(800)
                .setInterpolator(android.view.animation.LinearInterpolator())
                .start()
        }
    }

    private fun removeBugFromSandbox(bug: Bug, animate: Boolean) {
        val bugView = bugViewsMap[bug.id] ?: return
        activeBugsList.remove(bug)
        bugViewsMap.remove(bug.id)

        if (animate) {
            bugView.animate()
                .alpha(0f)
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        bugSpawnArea.removeView(bugView)
                    }
                })
                .start()
        } else {
            bugSpawnArea.removeView(bugView)
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
