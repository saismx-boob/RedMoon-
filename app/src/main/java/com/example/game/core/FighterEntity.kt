package com.example.game.core

import com.example.game.model.CharacterDef

/**
 * Live fighting entity in the 2D arena.
 */
class FighterEntity(
    val character: CharacterDef,
    var isPlayer: Boolean,
    startX: Float
) {
    var x: Float = startX
    var y: Float = 0f // 0 is ground level
    var vx: Float = 0f
    var vy: Float = 0f
    
    var health: Float = character.maxHealth
    var meter: Float = 25f // EX / Super Meter (0 to 100)
    var roundsWon: Int = 0
    var facingRight: Boolean = isPlayer

    var state: FighterState = FighterState.IDLE
    var stateTime: Float = 0f
    var currentAttack: MoveData? = null
    var attackFrameIndex: Int = 0
    var hitConfirmed: Boolean = false
    var hitStunRemaining: Int = 0
    var blockStunRemaining: Int = 0
    var isAirborne: Boolean = false
    var isCounterHitState: Boolean = false

    // Dimensions
    val bodyWidth: Float = 90f * character.visualScale
    val bodyHeight: Float = 220f * character.visualScale

    fun getHurtBox(): BoundingBox {
        val halfW = bodyWidth / 2f
        val height = if (state in listOf(FighterState.CROUCH, FighterState.BLOCK_CROUCH)) {
            bodyHeight * 0.65f
        } else {
            bodyHeight
        }
        return BoundingBox(
            left = x - halfW,
            top = y + height,
            right = x + halfW,
            bottom = y
        )
    }

    fun getHitBox(): BoundingBox? {
        val attack = currentAttack ?: return null
        if (!attack.isActive(attackFrameIndex) || hitConfirmed) return null

        val dir = if (facingRight) 1f else -1f
        val reach = attack.reach
        val startX = x + dir * (bodyWidth * 0.3f)
        val endX = startX + dir * reach

        val left = minOf(startX, endX)
        val right = maxOf(startX, endX)
        val bottom = y + attack.heightOffset - (attack.hitboxHeight / 2f)
        val top = bottom + attack.hitboxHeight

        return BoundingBox(left, top, right, bottom)
    }

    fun getPushBox(): BoundingBox {
        val halfW = bodyWidth * 0.35f
        return BoundingBox(x - halfW, y + bodyHeight, x + halfW, y)
    }

    fun resetForRound(startX: Float, initialFacingRight: Boolean) {
        x = startX
        y = 0f
        vx = 0f
        vy = 0f
        health = character.maxHealth
        state = FighterState.IDLE
        stateTime = 0f
        currentAttack = null
        attackFrameIndex = 0
        hitConfirmed = false
        hitStunRemaining = 0
        blockStunRemaining = 0
        isAirborne = false
        facingRight = initialFacingRight
        isCounterHitState = false
    }

    fun startAttack(move: MoveData): Boolean {
        // Super Art check
        if (move.type == AttackType.SUPER) {
            if (meter < 100f) return false
            meter -= 100f
        }

        currentAttack = move
        attackFrameIndex = 0
        hitConfirmed = false
        stateTime = 0f
        state = when (move.type) {
            AttackType.LIGHT_PUNCH -> FighterState.LIGHT_PUNCH
            AttackType.HEAVY_PUNCH -> FighterState.HEAVY_PUNCH
            AttackType.LIGHT_KICK -> FighterState.LIGHT_KICK
            AttackType.HEAVY_KICK -> FighterState.HEAVY_KICK
            AttackType.SPECIAL -> FighterState.SPECIAL_MOVE
            AttackType.SUPER -> FighterState.SUPER_ART
        }
        isCounterHitState = true
        return true
    }
}
