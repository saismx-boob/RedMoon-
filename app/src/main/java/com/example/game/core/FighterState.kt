package com.example.game.core

/**
 * State machine for 2D fighting game characters.
 */
enum class FighterState {
    IDLE,
    WALK_FORWARD,
    WALK_BACK,
    CROUCH,
    DASH_FORWARD,
    DASH_BACK,
    JUMP_UP,
    JUMP_FORWARD,
    JUMP_BACK,
    
    // Normal Attacks
    LIGHT_PUNCH,
    HEAVY_PUNCH,
    LIGHT_KICK,
    HEAVY_KICK,
    
    // Specials & Supers
    SPECIAL_MOVE,
    SUPER_ART,
    
    // Defense & Damage States
    BLOCK_STAND,
    BLOCK_CROUCH,
    HIT_STUN,
    BLOCK_STUN,
    KNOCKDOWN,
    WAKEUP,
    DEFEATED,
    VICTORY;

    val isAttacking: Boolean
        get() = this in listOf(
            LIGHT_PUNCH, HEAVY_PUNCH,
            LIGHT_KICK, HEAVY_KICK,
            SPECIAL_MOVE, SUPER_ART
        )

    val isBlocking: Boolean
        get() = this == BLOCK_STAND || this == BLOCK_CROUCH

    val isHitOrDown: Boolean
        get() = this in listOf(HIT_STUN, BLOCK_STUN, KNOCKDOWN, WAKEUP, DEFEATED)

    val canMove: Boolean
        get() = this in listOf(IDLE, WALK_FORWARD, WALK_BACK, CROUCH)

    val canCancelIntoSpecial: Boolean
        get() = this in listOf(LIGHT_PUNCH, HEAVY_PUNCH, LIGHT_KICK, HEAVY_KICK)

    val canCancelIntoSuper: Boolean
        get() = this in listOf(LIGHT_PUNCH, HEAVY_PUNCH, LIGHT_KICK, HEAVY_KICK, SPECIAL_MOVE)
}

enum class AttackType {
    LIGHT_PUNCH,
    HEAVY_PUNCH,
    LIGHT_KICK,
    HEAVY_KICK,
    SPECIAL,
    SUPER
}

enum class Direction {
    NEUTRAL,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    UP_LEFT,
    UP_RIGHT,
    DOWN_LEFT,
    DOWN_RIGHT
}
