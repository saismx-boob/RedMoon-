package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.game.core.AttackType
import com.example.game.core.MoveData

enum class Archetype {
    RUSHDOWN,
    TITAN_POWER,
    SHINOBI_SPEED,
    FLAME_DUELIST
}

enum class CostumePalette(val displayName: String, val tintColor: Color?) {
    CLASSIC_90S("Classic 90s Manga", null),
    CYBER_NEON("Cyber Neon", Color(0xFF67E8F9)),
    CRIMSON_FIRE("Crimson Fury", Color(0xFFFF5238)),
    GOLD_MASTER("Gold Champion", Color(0xFFFACC15))
}

data class SpecialMoveDef(
    val name: String,
    val commandHint: String,
    val description: String,
    val moveData: MoveData
)

data class CharacterDef(
    val id: String,
    val name: String,
    val subtitle: String,
    val bio: String,
    val archetype: Archetype,
    val fightingStyle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val portraitResId: Int,
    val idleSpriteResId: Int,
    val attackSpriteResId: Int,
    
    // Stats
    val maxHealth: Float = 100f,
    val walkSpeed: Float = 340f,
    val dashSpeed: Float = 580f,
    val jumpVelocity: Float = 720f,
    val gravity: Float = 1700f,
    val weight: Float = 1.0f,
    
    // Move frame data
    val lightPunch: MoveData,
    val heavyPunch: MoveData,
    val lightKick: MoveData,
    val heavyKick: MoveData,
    val specialMove: SpecialMoveDef,
    val superArt: SpecialMoveDef,
    
    // Visual traits
    val visualScale: Float = 1.0f,
    val quoteWin: String,
    val quoteLoss: String,
    val costume: CostumePalette = CostumePalette.CLASSIC_90S
)
