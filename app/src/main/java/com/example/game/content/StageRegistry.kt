package com.example.game.content

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.game.model.AmbientParticleType
import com.example.game.model.StageDef

object StageRegistry {

    val ASHEN_RUINS = StageDef(
        id = "ashen_ruins",
        name = "Ashen Ruins",
        subtitle = "STAGE 01 - CRUMBLING TEMPLE",
        backgroundResId = R.drawable.stage_ashen_ruins_1790969246126,
        floorY = 530f,
        ambientParticle = AmbientParticleType.VOLCANIC_EMBERS,
        primaryAtmosphereColor = Color(0xFFFF5238),
        musicMood = "Dramatic 90s volcanic synth rock"
    )

    val CYBER_EDO = StageDef(
        id = "cyber_edo",
        name = "Neo-Cyber Edo",
        subtitle = "STAGE 02 - RAIN ROOFTOP",
        backgroundResId = R.drawable.stage_cyber_edo_1790969258914,
        floorY = 530f,
        ambientParticle = AmbientParticleType.CYBER_RAIN,
        primaryAtmosphereColor = Color(0xFFA855F7),
        musicMood = "Fast-paced synthwave cyber funk"
    )

    val DRAGON_SHRINE = StageDef(
        id = "dragon_shrine",
        name = "Dragon Shrine",
        subtitle = "STAGE 03 - MOONLIT TORII",
        backgroundResId = R.drawable.stage_dragon_shrine_1790969273601,
        floorY = 530f,
        ambientParticle = AmbientParticleType.CHERRY_BLOSSOMS,
        primaryAtmosphereColor = Color(0xFF38BDF8),
        musicMood = "Traditional Japanese koto fighting melody"
    )

    val TRAINING_DOJO = StageDef(
        id = "training_dojo",
        name = "Training Dojo",
        subtitle = "DOJO - COMBAT PRACTICE",
        backgroundResId = R.drawable.stage_training_dojo_1790969287836,
        floorY = 530f,
        ambientParticle = AmbientParticleType.DOJO_DUST,
        primaryAtmosphereColor = Color(0xFFFACC15),
        musicMood = "Focused martial arts groove"
    )

    val ALL_STAGES = listOf(ASHEN_RUINS, CYBER_EDO, DRAGON_SHRINE, TRAINING_DOJO)

    fun getStage(id: String): StageDef {
        return ALL_STAGES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ASHEN_RUINS
    }
}
