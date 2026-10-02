package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class AmbientParticleType {
    VOLCANIC_EMBERS,
    CYBER_RAIN,
    CHERRY_BLOSSOMS,
    DOJO_DUST
}

data class StageDef(
    val id: String,
    val name: String,
    val subtitle: String,
    val backgroundResId: Int,
    val floorY: Float = 530f,
    val ambientParticle: AmbientParticleType,
    val primaryAtmosphereColor: Color,
    val musicMood: String
)

enum class GameMode {
    ARCADE,
    VERSUS,
    TRAINING,
    SURVIVAL
}

enum class AiDifficulty {
    EASY,
    NORMAL,
    HARD,
    MASTER
}

enum class DummyBehavior {
    STAND,
    CROUCH,
    GUARD_ALL,
    COUNTER_ATTACK
}
