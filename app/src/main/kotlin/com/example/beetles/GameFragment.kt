package com.example.beetles

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern
import kotlin.random.Random

class GameFragment : Fragment(R.layout.fragment_game), SensorEventListener {

    private val viewModel: GameViewModel by viewModel()

    private lateinit var gameContainer: RelativeLayout
    private lateinit var bugSpawnArea: RelativeLayout
    private lateinit var scoreText: TextView
    private lateinit var timeText: TextView
    private lateinit var btnStart: FloatingActionButton

    private val handler = Handler(Looper.getMainLooper())

    private val bugViewsMap = mutableMapOf<String, ImageView>()
    private val activeBugsList = mutableListOf<Bug>()

    private val bugRotationMap = mutableMapOf<String, Float>()
    private val bugRotationDirectionMap = mutableMapOf<String, Float>()

    private var spawnRunnable: Runnable? = null
    private var moveRunnable: Runnable? = null
    private var timerRunnable: Runnable? = null
    private var goldenBugRunnable: Runnable? = null
    private var bonusRunnable: Runnable? = null
    private var tiltExpirationRunnable: Runnable? = null
    private var isTiltModeActive = false
    private var tiltBonusView: ImageView? = null
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null
    private var tiltX = 0f
    private var tiltY = 0f

    private var soundPool: SoundPool? = null
    private val soundIds = mutableListOf<Int>()

    private val PREFS_NAME = "BeetlesGamePrefs"
    private val BONUS_DURATION_MS = 10000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

        loadSettings()
        initSound()
        initSensor()
        setupListeners()

        if (viewModel.isPlaying) {
            btnStart.setImageResource(android.R.drawable.ic_media_pause)
            updateUI()

            startSpawning()
            startMoving()
            startTimer()
            startBonusSpawner()
            startGoldenBugSpawner()
        } else {
            bugSpawnArea.post {
                startGame()
            }
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

    private fun initSound() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        val pool = soundPool ?: return
        val ctx = requireContext()

        val soundFiles = listOf(R.raw.bug_scream, R.raw.bug_scream_loud)

        soundFiles.forEach { resId ->
            val id = pool.load(ctx, resId, 1)
            if (id != 0) soundIds.add(id)
        }
    }

    private fun playRandomSound() {
        if (soundIds.isEmpty()) return
        val randomId = soundIds.random()
        soundPool?.play(randomId, 1f, 1f, 1, 0, 1f)
    }

    private fun initSensor() {
        val sm = requireContext().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager = sm
        gravitySensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        viewModel.gameSpeed = prefs.getInt("speed", 10)
        viewModel.maxInsectsLimit = prefs.getInt("max_insects", 20)

        if (!viewModel.isPlaying) {
            viewModel.setGameTime(prefs.getInt("duration", 60))
        }
    }

    private fun setupListeners() {
        bugSpawnArea.setOnClickListener {
            if (viewModel.isPlaying) {
                viewModel.addScore(-5)
                updateUI()
                Toast.makeText(requireContext(), "Промах! -5 очков", Toast.LENGTH_SHORT).show()
            }
        }

        btnStart.setOnClickListener {
            if (viewModel.isPlaying) stopGame() else startGame()
        }
    }

    private fun startGame() {
        viewModel.setPlaying(true)
        viewModel.reset()

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        viewModel.setGameTime(prefs.getInt("duration", 60))

        loadSettings()
        updateUI()

        btnStart.setImageResource(android.R.drawable.ic_media_pause)
        bugSpawnArea.removeAllViews()
        bugViewsMap.clear()
        activeBugsList.clear()

        bugRotationMap.clear()
        bugRotationDirectionMap.clear()
        isTiltModeActive = false
        tiltBonusView = null
        tiltX = 0f
        tiltY = 0f

        startSpawning()
        startMoving()
        startTimer()
        startBonusSpawner()
        startGoldenBugSpawner()
    }

    private fun startGoldenBugSpawner() {
        goldenBugRunnable = object : Runnable {
            override fun run() {
                if (viewModel.isPlaying) {
                    spawnGoldenBug()
                    handler.postDelayed(this, 20000L)
                }
            }
        }
        handler.postDelayed(goldenBugRunnable!!, 20000L)
    }

    private fun spawnGoldenBug() {
        if (activeBugsList.size >= viewModel.maxInsectsLimit) return

        val bugEntity = Bug(
            id = java.util.UUID.randomUUID().toString(),
            logicalX = Random.nextFloat() * 0.8f,
            logicalY = Random.nextFloat() * 0.8f,
            speed = 1.4f,
            logicalSize = 0.10f,
            type = BugType.GOLD,
            visualResource = R.drawable.bug_gold
        )
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
            if (viewModel.isPlaying) {
                fetchGoldPriceAndAddScore(bugEntity)
                removeBugFromSandbox(bugEntity, animate = true)
            }
        }

        bugViewsMap[bugEntity.id] = bugView
        bugRotationMap[bugEntity.id] = 0f
        bugRotationDirectionMap[bugEntity.id] = if (Random.nextBoolean()) 1f else -1f

        bugSpawnArea.addView(bugView)

        bugView.alpha = 0f
        bugView.scaleX = 0f
        bugView.scaleY = 0f
        bugView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()

        handler.postDelayed({
            if (activeBugsList.contains(bugEntity)) {
                removeBugFromSandbox(bugEntity, animate = true)
            }
        }, 7000L)
    }

    private fun fetchGoldPriceAndAddScore(bug: Bug) {
        lifecycleScope.launch {
            val scoreResult = withContext(Dispatchers.IO) {
                try {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val endDate = sdf.format(java.util.Date())
                    val startDate = sdf.format(java.util.Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000))

                    val responseCall = GoldApi.create().getGoldPrice(startDate, endDate)

                    val responseString = responseCall.execute().body() ?: ""

                    val matcher = Pattern.compile("<Record[^>]*Code=\"1\"[^>]*>.*?<Buy>([^<]+)</Buy>", Pattern.DOTALL).matcher(responseString)
                    if (matcher.find()) {
                        val priceString = matcher.group(1).replace(",", ".").replace(" ", "")
                        priceString.toDouble().toInt() / 100
                    } else {
                        50
                    }
                } catch (e: Exception) {
                    50
                }
            }
            if (viewModel.isPlaying) {
                viewModel.addScore(scoreResult)
                updateUI()
                Toast.makeText(requireContext(), "Золотой жук пойман! +$scoreResult очков", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBonusSpawner() {
        bonusRunnable = object : Runnable {
            override fun run() {
                if (viewModel.isPlaying) {
                    spawnBonus()
                    handler.postDelayed(this, 15000L)
                }
            }
        }
        handler.post(bonusRunnable!!)
    }

    private fun spawnBonus() {
        tiltBonusView?.let { bugSpawnArea.removeView(it) }

        val bonusSize = 150
        val bonusView = ImageView(requireContext()).apply {
            setImageResource(R.drawable.bonus)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = RelativeLayout.LayoutParams(bonusSize, bonusSize)

            val maxX = (bugSpawnArea.width - bonusSize).coerceAtLeast(0)
            val maxY = (bugSpawnArea.height - bonusSize).coerceAtLeast(0)
            translationX = Random.nextInt(0, maxX).toFloat()
            translationY = Random.nextInt(0, maxY).toFloat()
        }
        bonusView.setOnClickListener {
            activateTiltMode()
            bugSpawnArea.removeView(bonusView)
            tiltBonusView = null
        }
        bugSpawnArea.addView(bonusView)
        tiltBonusView = bonusView
        bonusView.alpha = 0f
        bonusView.scaleX = 0f
        bonusView.scaleY = 0f
        bonusView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start()
        handler.postDelayed({
            if (tiltBonusView == bonusView) {
                bugSpawnArea.removeView(bonusView)
                tiltBonusView = null
            }
        }, 5000L)
    }
    private fun activateTiltMode() {
        isTiltModeActive = true
        Toast.makeText(requireContext(), "Режим наклона активирован на 10 сек!", Toast.LENGTH_SHORT).show()
        playRandomSound()
        val sensor = gravitySensor
        if (sensor != null && sensorManager != null) {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        tiltExpirationRunnable = Runnable {
            if (viewModel.isPlaying && isTiltModeActive) {
                deactivateTiltMode()
            }
        }
        handler.postDelayed(tiltExpirationRunnable!!, BONUS_DURATION_MS)
    }
    private fun deactivateTiltMode() {
        isTiltModeActive = false
        sensorManager?.unregisterListener(this)
        tiltExpirationRunnable?.let { handler.removeCallbacks(it) }
        Toast.makeText(requireContext(), "Режим наклона завершен", Toast.LENGTH_SHORT).show()
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (!viewModel.isPlaying || !isTiltModeActive) return
        if (event?.sensor?.type != Sensor.TYPE_GRAVITY && event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val values = event.values
        if (values.size < 3) return
        val ax = values[0]
        val ay = values[1]
        val az = values[2]
        tiltX = ax
        tiltY = ay
        if (Math.abs(az) > 5f) {
            val factor = 9.8f / Math.abs(az).coerceAtLeast(1f)
            tiltX = ax * factor
            tiltY = ay * factor
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    private fun startSpawning() {
        spawnRunnable = object : Runnable {
            override fun run() {
                if (viewModel.isPlaying) {
                    spawnLogicalBug()
                    val interval = (1500 - (viewModel.gameSpeed * 140)).toLong().coerceAtLeast(100L)
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(spawnRunnable!!)
    }
    private fun startMoving() {
        moveRunnable = object : Runnable {
            override fun run() {
                if (viewModel.isPlaying) {
                    if (isTiltModeActive) {
                        moveBugsWithTilt()
                        handler.postDelayed(this, 16L)
                    } else {
                        moveLogicalBugsSmoothly()
                        handler.postDelayed(this, 330L)
                    }
                }
            }
        }
        handler.post(moveRunnable!!)
    }
    private fun moveBugsWithTilt() {
        val areaWidth = bugSpawnArea.width.toFloat()
        val areaHeight = bugSpawnArea.height.toFloat()
        val tiltMultiplier = 2.5f
        val rotationSpeed = 2.5f
        activeBugsList.forEach { bugEntity ->
            val bugView = bugViewsMap[bugEntity.id] ?: return@forEach
            val viewWidth = bugView.width.toFloat().coerceAtLeast(1f)
            val viewHeight = bugView.height.toFloat().coerceAtLeast(1f)
            var newX = bugView.translationX + tiltX * tiltMultiplier
            var newY = bugView.translationY + tiltY * tiltMultiplier
            val maxX = areaWidth - viewWidth
            val maxY = areaHeight - viewHeight
            var hitWall = false
            if (newX < 0f) { newX = 0f; hitWall = true }
            else if (newX > maxX) { newX = maxX; hitWall = true }
            if (newY < 0f) { newY = 0f; hitWall = true }
            else if (newY > maxY) { newY = maxY; hitWall = true }
            bugView.translationX = newX
            bugView.translationY = newY
            val speed = Math.sqrt((tiltX * tiltX + tiltY * tiltY).toDouble()).toFloat()
            if (speed > 0.5f && !hitWall) {
                val currentRotation = bugRotationMap.getOrDefault(bugEntity.id, 0f)
                val direction = bugRotationDirectionMap.getOrDefault(bugEntity.id, 1f)
                val newRotation = currentRotation + (speed * rotationSpeed * direction)
                bugRotationMap[bugEntity.id] = newRotation
                bugView.rotation = newRotation
            }
        }
    }
    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (viewModel.isPlaying) {
                    viewModel.decreaseTime()
                    updateUI()
                    if (viewModel.gameTime <= 0) stopGame() else handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.post(timerRunnable!!)
    }
    private fun spawnLogicalBug() {
        if (activeBugsList.size >= viewModel.maxInsectsLimit) return
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
            if (viewModel.isPlaying) {
                viewModel.addScore(bugEntity.type.scoreValue)
                updateUI()
                removeBugFromSandbox(bugEntity, animate = true)
            }
        }
        bugViewsMap[bugEntity.id] = bugView
        bugRotationMap[bugEntity.id] = 0f
        bugRotationDirectionMap[bugEntity.id] = if (Random.nextBoolean()) 1f else -1f
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
            bugEntity.calculateNextLogicalPosition(viewModel.gameSpeed)
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
        bugRotationMap.remove(bug.id)
        bugRotationDirectionMap.remove(bug.id)
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
        scoreText.text = "Очки: ${viewModel.score}"
        val mins = viewModel.gameTime / 60
        val secs = viewModel.gameTime % 60
        timeText.text = "Время: ${mins}:${secs.toString().padStart(2, '0')}"
    }
    private fun stopGame() {
        viewModel.setPlaying(false)
        isTiltModeActive = false
        btnStart.setImageResource(android.R.drawable.ic_media_play)
        spawnRunnable?.let { handler.removeCallbacks(it) }
        moveRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable?.let { handler.removeCallbacks(it) }
        bonusRunnable?.let { handler.removeCallbacks(it) }
        tiltExpirationRunnable?.let { handler.removeCallbacks(it) }
        goldenBugRunnable?.let { handler.removeCallbacks(it) }
        sensorManager?.unregisterListener(this)
        activeBugsList.toList().forEach { removeBugFromSandbox(it, animate = true) }
        tiltBonusView?.let { bugSpawnArea.removeView(it) }
        tiltBonusView = null
        Toast.makeText(requireContext(), "Игра окончена! Ваш счет: ${viewModel.score}", Toast.LENGTH_LONG).show()
        if (viewModel.score > 0) {
            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val playerId = prefs.getInt("current_player_id", 0)
            val preset = prefs.getString("difficulty_preset", "Средний") ?: "Средний"
            val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            if (playerId != 0) {
                val db = AppDatabase.getDatabase(requireContext())
                val bestRecord = db.recordDao().getRecordByPlayer(playerId)
                if (bestRecord == null || viewModel.score > bestRecord.score) {
                    val newRecord = PlayerRecord(
                        playerId = playerId,
                        score = viewModel.score,
                        difficultyPreset = preset,
                        dateString = dateStr
                    )
                    db.recordDao().insertOrUpdateRecord(newRecord)
                    if (bestRecord != null) {
                        Toast.makeText(requireContext(), "🎉 Новый рекорд обновлен!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        requireActivity().supportFragmentManager.popBackStack()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        spawnRunnable?.let { handler.removeCallbacks(it) }
        moveRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable?.let { handler.removeCallbacks(it) }
        bonusRunnable?.let { handler.removeCallbacks(it) }
        tiltExpirationRunnable?.let { handler.removeCallbacks(it) }
        goldenBugRunnable?.let { handler.removeCallbacks(it) }
        sensorManager?.unregisterListener(this)
        soundPool?.release()
        soundPool = null
        soundIds.clear()
    }
}