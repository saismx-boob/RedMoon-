package com.example.game.core

import com.example.game.model.AiDifficulty
import kotlin.random.Random

/**
 * Intelligent 2D Fighting Game AI Engine.
 * Evaluates spacing, frame advantage, anti-airs, block punishes, and combo confirmations.
 */
class FighterAI(
    var difficulty: AiDifficulty = AiDifficulty.NORMAL
) {
    private var thinkTimer: Float = 0f
    private var actionDuration: Float = 0f

    fun decide(
        aiFighter: FighterEntity,
        playerFighter: FighterEntity,
        dt: Float,
        onInput: (AiCommand) -> Unit
    ) {
        thinkTimer -= dt
        actionDuration -= dt

        if (aiFighter.state.isAttacking || aiFighter.state.isHitOrDown) {
            return
        }

        if (thinkTimer > 0f) return

        val distance = kotlin.math.abs(aiFighter.x - playerFighter.x)
        val isPlayerInAir = playerFighter.y > 20f
        val isPlayerAttacking = playerFighter.state.isAttacking
        val hasSuperReady = aiFighter.meter >= 100f
        val reactionSpeed = when (difficulty) {
            AiDifficulty.EASY -> 0.45f + Random.nextFloat() * 0.35f
            AiDifficulty.NORMAL -> 0.22f + Random.nextFloat() * 0.20f
            AiDifficulty.HARD -> 0.12f + Random.nextFloat() * 0.12f
            AiDifficulty.MASTER -> 0.05f + Random.nextFloat() * 0.07f
        }
        thinkTimer = reactionSpeed

        // 1. Anti-Air check: if player is jumping in close range, execute anti-air
        if (isPlayerInAir && distance < 200f && Random.nextFloat() < antiAirChance()) {
            onInput(AiCommand.HEAVY_KICK) // Launcher / Rising Anti-Air
            return
        }

        // 2. Defensive block reaction
        if (isPlayerAttacking && distance < 240f && Random.nextFloat() < blockChance()) {
            val blockLow = playerFighter.currentAttack?.isLow == true
            if (blockLow) {
                onInput(AiCommand.BLOCK_LOW)
            } else {
                onInput(AiCommand.BLOCK_HIGH)
            }
            actionDuration = 0.35f
            return
        }

        // 3. Super Art execution on opening or punish
        if (hasSuperReady && distance < 260f && Random.nextFloat() < superArtChance()) {
            onInput(AiCommand.SUPER_ART)
            return
        }

        // 4. Spacing and neutral game
        when {
            // Far distance: Walk forward, dash, or throw projectile
            distance > 380f -> {
                if (Random.nextFloat() < 0.45f) {
                    onInput(AiCommand.SPECIAL_MOVE) // Fireball / Range
                } else if (Random.nextFloat() < 0.5f) {
                    onInput(AiCommand.DASH_FORWARD)
                } else {
                    onInput(AiCommand.WALK_FORWARD)
                }
            }

            // Medium distance: Poke or dash in
            distance in 210f..380f -> {
                val roll = Random.nextFloat()
                when {
                    roll < 0.35f -> onInput(AiCommand.SPECIAL_MOVE)
                    roll < 0.65f -> onInput(AiCommand.HEAVY_PUNCH) // Long poke
                    roll < 0.85f -> onInput(AiCommand.WALK_FORWARD)
                    else -> onInput(AiCommand.WALK_BACK)
                }
            }

            // Close range: Combo initiation (Light Punch -> confirm into combo)
            else -> {
                val roll = Random.nextFloat()
                when {
                    roll < 0.35f -> onInput(AiCommand.LIGHT_PUNCH)
                    roll < 0.60f -> onInput(AiCommand.LIGHT_KICK)
                    roll < 0.80f -> onInput(AiCommand.HEAVY_KICK)
                    else -> onInput(AiCommand.HEAVY_PUNCH)
                }
            }
        }
    }

    private fun blockChance(): Float = when (difficulty) {
        AiDifficulty.EASY -> 0.15f
        AiDifficulty.NORMAL -> 0.45f
        AiDifficulty.HARD -> 0.75f
        AiDifficulty.MASTER -> 0.92f
    }

    private fun antiAirChance(): Float = when (difficulty) {
        AiDifficulty.EASY -> 0.10f
        AiDifficulty.NORMAL -> 0.40f
        AiDifficulty.HARD -> 0.70f
        AiDifficulty.MASTER -> 0.90f
    }

    private fun superArtChance(): Float = when (difficulty) {
        AiDifficulty.EASY -> 0.25f
        AiDifficulty.NORMAL -> 0.55f
        AiDifficulty.HARD -> 0.80f
        AiDifficulty.MASTER -> 0.95f
    }
}

enum class AiCommand {
    WALK_FORWARD,
    WALK_BACK,
    DASH_FORWARD,
    DASH_BACK,
    JUMP,
    CROUCH,
    LIGHT_PUNCH,
    HEAVY_PUNCH,
    LIGHT_KICK,
    HEAVY_KICK,
    SPECIAL_MOVE,
    SUPER_ART,
    BLOCK_HIGH,
    BLOCK_LOW
}
