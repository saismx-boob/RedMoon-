package com.example.game.core

/**
 * Geometric bounding box for collision detection in 2D combat.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun intersects(other: BoundingBox): Boolean {
        return left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
    }

    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

/**
 * Result of a collision check between attacker and defender.
 */
data class HitResult(
    val isHit: Boolean,
    val isBlocked: Boolean,
    val isCounterHit: Boolean,
    val damage: Float,
    val hitStun: Int,
    val blockStun: Int,
    val pushback: Float,
    val isLauncher: Boolean,
    val isKnockdown: Boolean,
    val impactX: Float,
    val impactY: Float
)
