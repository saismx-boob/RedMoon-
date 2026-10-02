package com.example.game.core

/**
 * Manages combo tracking, damage scaling, juggles, and performance rankings.
 */
class ComboTracker {
    var hitCount: Int = 0
        private set
    var totalDamage: Float = 0f
        private set
    var juggleCount: Int = 0
        private set
    var comboTimer: Float = 0f
        private set
    var highestCombo: Int = 0
        private set

    val isActive: Boolean get() = hitCount >= 2 && comboTimer > 0f

    val ratingText: String
        get() = when {
            hitCount >= 10 -> "RIFT BREAKER!"
            hitCount >= 8 -> "ULTRA COMBO!"
            hitCount >= 6 -> "SUPER COMBO!"
            hitCount >= 4 -> "GREAT COMBO!"
            hitCount >= 2 -> "GOOD COMBO!"
            else -> ""
        }

    fun registerHit(rawDamage: Float, isJuggle: Boolean): Float {
        hitCount++
        if (hitCount > highestCombo) highestCombo = hitCount
        if (isJuggle) juggleCount++
        comboTimer = 1.35f // Reset combo timeout window

        // Damage scaling formula: 1st hit 100%, 2nd 90%, 3rd 80%, etc. Floor at 25%
        val scaleMultiplier = (1.0f - (hitCount - 1) * 0.10f).coerceIn(0.25f, 1.0f)
        val scaledDamage = rawDamage * scaleMultiplier
        totalDamage += scaledDamage
        return scaledDamage
    }

    fun update(dt: Float) {
        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                reset()
            }
        }
    }

    fun reset() {
        hitCount = 0
        totalDamage = 0f
        juggleCount = 0
        comboTimer = 0f
    }
}
