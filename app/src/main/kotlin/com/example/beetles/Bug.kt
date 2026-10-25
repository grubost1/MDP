package com.example.beetles

import kotlin.random.Random

enum class BugType(val scoreValue: Int) {
    NORMAL(5),
    FAST(10),
    RARE(25)
}

data class Bug(
    val id: String,
    var logicalX: Float,
    var logicalY: Float,
    val speed: Float,
    val logicalSize: Float,
    val type: BugType,
    val visualResource: Int
) {
    fun calculateNextLogicalPosition(globalGameSpeed: Int) {
        val jumpRange = 0.03f * globalGameSpeed * speed

        val deltaX = Random.nextFloat() * (jumpRange * 2) - jumpRange
        val deltaY = Random.nextFloat() * (jumpRange * 2) - jumpRange

        val maxLogicalPos = 1.0f - logicalSize
        logicalX = (logicalX + deltaX).coerceIn(0.0f, maxLogicalPos)
        logicalY = (logicalY + deltaY).coerceIn(0.0f, maxLogicalPos)
    }

    companion object {
        fun createRandomBug(availableDrawables: List<Int>): Bug {
            val id = java.util.UUID.randomUUID().toString()

            val startX = Random.nextFloat() * 0.8f
            val startY = Random.nextFloat() * 0.8f

            val visualRes = availableDrawables.random()

            val (type, speed, size) = when (visualRes) {
                R.drawable.bug_blue -> Triple(BugType.FAST, 1.8f, 0.12f)
                R.drawable.bug_dr -> Triple(BugType.RARE, 1.2f, 0.10f)
                else -> Triple(BugType.NORMAL, 1f, 0.15f)
            }

            return Bug(
                id = id,
                logicalX = startX,
                logicalY = startY,
                speed = speed,
                logicalSize = size,
                type = type,
                visualResource = visualRes
            )
        }
    }
}
