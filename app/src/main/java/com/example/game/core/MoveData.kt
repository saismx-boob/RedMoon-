package com.example.game.core

/**
 * Data structure defining exact attack frame data and combat mechanics for 2D fighting games.
 * Follows classic Street Fighter / Guilty Gear frame standards.
 */
data class MoveData(
    val name: String,
    val type: AttackType,
    val startupFrames: Int,    // Startup before hitbox becomes active
    val activeFrames: Int,     // Frames hitbox is damaging
    val recoveryFrames: Int,   // Vulnerable frames after active
    val damage: Float,
    val meterGain: Float,
    val meterCost: Float = 0f,
    val hitStunFrames: Int,
    val blockStunFrames: Int,
    val pushback: Float,
    val reach: Float,          // Hitbox width
    val heightOffset: Float,   // Hitbox vertical offset from fighter base
    val hitboxHeight: Float,   // Hitbox vertical height
    val isLow: Boolean = false,
    val isOverhead: Boolean = false,
    val isLauncher: Boolean = false, // Launches opponent into aerial juggle
    val isKnockdown: Boolean = false,
    val spawnsProjectile: Boolean = false,
    val canCancelIntoSpecial: Boolean = true,
    val canCancelIntoSuper: Boolean = true,
    val soundKey: String = "hit_light"
) {
    val totalFrames: Int = startupFrames + activeFrames + recoveryFrames
    
    // Check if current frame index is within the active hit window
    fun isActive(frameIndex: Int): Boolean {
        return frameIndex in startupFrames until (startupFrames + activeFrames)
    }

    // Check if current frame can cancel on hit
    fun canCancel(frameIndex: Int, hitRegistered: Boolean): Boolean {
        return hitRegistered && frameIndex >= startupFrames
    }
}
