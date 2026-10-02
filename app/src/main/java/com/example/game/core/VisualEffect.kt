package com.example.game.core

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Types of particles and visual effects in the 2D combat arena.
 */
enum class ParticleType {
    SPARK,
    FLAME_BURST,
    RIFT_ENERGY,
    BLOCK_CLANG,
    DUST,
    SLASH_ARC,
    MAGMA_EMBER,
    SHOCKWAVE
}

data class CombatParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Color,
    var alpha: Float = 1f,
    var life: Float,
    val maxLife: Float,
    val type: ParticleType = ParticleType.SPARK
)

data class FloatingCombatText(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    val isCritical: Boolean = false,
    var alpha: Float = 1f,
    var life: Float = 1.0f
)

data class AfterImage(
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    val color: Color,
    var alpha: Float = 0.6f,
    var life: Float = 0.25f,
    val state: FighterState
)

data class Projectile(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    val width: Float,
    val height: Float,
    val damage: Float,
    val isPlayer: Boolean,
    val color: Color,
    val effectType: String,
    var life: Float = 2.5f
)

class VisualEffectsManager {
    val particles = mutableListOf<CombatParticle>()
    val floatingTexts = mutableListOf<FloatingCombatText>()
    val afterImages = mutableListOf<AfterImage>()
    
    var screenShakeAmount: Float = 0f
    var hitStopFrames: Int = 0
    var superFlashActive: Boolean = false
    var superFlashTimer: Float = 0f

    fun update(dt: Float) {
        if (screenShakeAmount > 0f) {
            screenShakeAmount = (screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        }

        if (superFlashTimer > 0f) {
            superFlashTimer = (superFlashTimer - dt).coerceAtLeast(0f)
            superFlashActive = superFlashTimer > 0f
        }

        // Update particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.type != ParticleType.SLASH_ARC) {
                p.vy += 450f * dt // gravity
            }
            p.life -= dt
            p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.life <= 0f) pIter.remove()
        }

        // Update floating texts
        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val t = tIter.next()
            t.y -= 45f * dt
            t.life -= dt
            t.alpha = (t.life / 0.8f).coerceIn(0f, 1f)
            if (t.life <= 0f) tIter.remove()
        }

        // Update after-images
        val aIter = afterImages.iterator()
        while (aIter.hasNext()) {
            val a = aIter.next()
            a.life -= dt
            a.alpha = (a.life / 0.25f).coerceIn(0f, 1f)
            if (a.life <= 0f) aIter.remove()
        }
    }

    fun spawnHitSparks(x: Float, y: Float, count: Int, baseColor: Color, isHeavy: Boolean = false) {
        screenShakeAmount = if (isHeavy) 12f else 6f
        repeat(count) {
            val angle = Random.nextFloat() * (Math.PI.toFloat() * 2f)
            val speed = Random.nextFloat() * (if (isHeavy) 480f else 280f) + 80f
            val life = Random.nextFloat() * 0.25f + 0.15f
            particles.add(
                CombatParticle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = if (isHeavy) Random.nextFloat() * 6f + 4f else Random.nextFloat() * 4f + 2f,
                    color = baseColor,
                    life = life,
                    maxLife = life,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    fun spawnBlockSparks(x: Float, y: Float) {
        screenShakeAmount = 3f
        repeat(8) {
            val angle = Random.nextFloat() * Math.PI.toFloat() - (Math.PI.toFloat() / 2f)
            val speed = Random.nextFloat() * 220f + 50f
            val life = 0.2f
            particles.add(
                CombatParticle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = 3.5f,
                    color = Color(0xFFE2E8F0),
                    life = life,
                    maxLife = life,
                    type = ParticleType.BLOCK_CLANG
                )
            )
        }
    }

    fun spawnDust(x: Float, groundY: Float, count: Int = 5) {
        repeat(count) {
            val speedX = (Random.nextFloat() - 0.5f) * 160f
            val speedY = -Random.nextFloat() * 70f
            val life = 0.35f
            particles.add(
                CombatParticle(
                    x = x,
                    y = groundY - 4f,
                    vx = speedX,
                    vy = speedY,
                    size = Random.nextFloat() * 6f + 3f,
                    color = Color(0xFF8C867A),
                    life = life,
                    maxLife = life,
                    type = ParticleType.DUST
                )
            )
        }
    }

    fun spawnSuperArtFlash(x: Float, y: Float, superColor: Color) {
        superFlashActive = true
        superFlashTimer = 0.45f
        screenShakeAmount = 18f
        repeat(30) {
            val angle = Random.nextFloat() * (Math.PI.toFloat() * 2f)
            val speed = Random.nextFloat() * 600f + 150f
            val life = 0.45f
            particles.add(
                CombatParticle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    size = Random.nextFloat() * 9f + 4f,
                    color = superColor,
                    life = life,
                    maxLife = life,
                    type = ParticleType.SHOCKWAVE
                )
            )
        }
    }

    fun addFloatingText(x: Float, y: Float, text: String, color: Color, isCrit: Boolean = false) {
        floatingTexts.add(FloatingCombatText(x, y - 20f, text, color, isCrit))
    }

    fun addAfterImage(x: Float, y: Float, facingRight: Boolean, color: Color, state: FighterState) {
        afterImages.add(AfterImage(x, y, facingRight, color, 0.6f, 0.22f, state))
    }

    fun clear() {
        particles.clear()
        floatingTexts.clear()
        afterImages.clear()
        screenShakeAmount = 0f
        hitStopFrames = 0
        superFlashActive = false
        superFlashTimer = 0f
    }
}
