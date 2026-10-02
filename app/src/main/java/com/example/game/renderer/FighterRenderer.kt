package com.example.game.renderer

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.game.core.*
import com.example.game.model.AmbientParticleType
import com.example.game.model.CostumePalette
import com.example.game.model.StageDef
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * High-performance 2D Canvas rendering engine for 90s anime fighting game characters,
 * sprite loading, layered particle effects, stage ambiances, and dynamic hitboxes.
 */
class FighterRenderer(private val context: Context? = null) {

    private val bitmapCache = mutableMapOf<Int, ImageBitmap>()

    fun getBitmap(resId: Int): ImageBitmap? {
        if (resId == 0 || context == null) return null
        bitmapCache[resId]?.let { return it }
        return try {
            val bmp = BitmapFactory.decodeResource(context.resources, resId)
            bmp?.asImageBitmap()?.also { bitmapCache[resId] = it }
        } catch (_: Exception) {
            null
        }
    }

    fun drawStageAmbience(
        drawScope: DrawScope,
        stage: StageDef,
        elapsedTime: Float,
        arenaWidth: Float,
        arenaHeight: Float
    ) {
        val count = 28
        when (stage.ambientParticle) {
            AmbientParticleType.VOLCANIC_EMBERS -> {
                for (i in 0 until count) {
                    val x = ((i * 137.5f + sin(elapsedTime * 0.4f + i) * 35f) % arenaWidth + arenaWidth) % arenaWidth
                    val y = arenaHeight - (((elapsedTime * (30f + (i % 5) * 15f) + i * 55f) % arenaHeight + arenaHeight) % arenaHeight)
                    val alpha = (0.3f + sin(elapsedTime * 2.5f + i) * 0.3f).coerceIn(0.1f, 0.8f)
                    val color = if (i % 3 == 0) Color(0xFFFDE047) else Color(0xFFFF5238)
                    drawScope.drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = if (i % 2 == 0) 3.5f else 2f,
                        center = Offset(x, y)
                    )
                }
            }
            AmbientParticleType.CYBER_RAIN -> {
                for (i in 0 until 40) {
                    val x = ((i * 85f + sin(i.toFloat()) * 50f) % arenaWidth + arenaWidth) % arenaWidth
                    val y = ((elapsedTime * 850f + i * 40f) % arenaHeight + arenaHeight) % arenaHeight
                    drawScope.drawLine(
                        color = Color(0xFF67E8F9).copy(alpha = 0.45f),
                        start = Offset(x, y),
                        end = Offset(x - 8f, y + 25f),
                        strokeWidth = 1.5f
                    )
                }
            }
            AmbientParticleType.CHERRY_BLOSSOMS -> {
                for (i in 0 until count) {
                    val x = ((elapsedTime * (40f + (i % 3) * 20f) + i * 95f) % arenaWidth + arenaWidth) % arenaWidth
                    val y = ((elapsedTime * (50f + (i % 4) * 15f) + i * 65f + sin(elapsedTime + i) * 30f) % arenaHeight + arenaHeight) % arenaHeight
                    val color = Color(0xFFF472B6).copy(alpha = 0.65f)
                    drawScope.drawOval(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(8f, 5f)
                    )
                }
            }
            AmbientParticleType.DOJO_DUST -> {
                for (i in 0 until 20) {
                    val x = ((i * 120f + sin(elapsedTime * 0.2f + i) * 40f) % arenaWidth + arenaWidth) % arenaWidth
                    val y = arenaHeight - (((elapsedTime * 15f + i * 45f) % arenaHeight + arenaHeight) % arenaHeight)
                    drawScope.drawCircle(
                        color = Color(0xFFFEF08A).copy(alpha = 0.35f),
                        radius = 2.5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }

    fun drawFighter(
        drawScope: DrawScope,
        fighter: FighterEntity,
        groundY: Float,
        elapsedTime: Float,
        showHitboxes: Boolean = false
    ) {
        val footX = fighter.x
        val footY = groundY - fighter.y
        val dir = if (fighter.facingRight) 1f else -1f

        // 1. Draw floor shadow
        val shadowW = (fighter.bodyWidth * 1.4f) * (1f - (fighter.y / 800f)).coerceIn(0.2f, 1f)
        val shadowAlpha = (0.55f * (1f - (fighter.y / 600f))).coerceIn(0.1f, 0.55f)
        drawScope.drawOval(
            color = Color.Black.copy(alpha = shadowAlpha),
            topLeft = Offset(footX - shadowW / 2f, groundY - 12f),
            size = Size(shadowW, 24f)
        )

        // 2. Select Sprite: Idle or Attack
        val isAttacking = fighter.state.isAttacking
        val spriteResId = if (isAttacking) fighter.character.attackSpriteResId else fighter.character.idleSpriteResId
        val spriteBitmap = getBitmap(spriteResId)

        val breath = if (fighter.state == FighterState.IDLE) sin(elapsedTime * 4f) * 3f else 0f

        if (spriteBitmap != null) {
            // Draw real full-body 90s manga arcade character sprite!
            drawSpriteCharacter(drawScope, fighter, spriteBitmap, footX, footY + breath, dir)
        } else {
            // Fallback Vector Rendering
            val walkCycle = if (fighter.state in listOf(FighterState.WALK_FORWARD, FighterState.WALK_BACK)) {
                sin(elapsedTime * 14f) * 8f
            } else 0f
            drawFallbackVector(drawScope, fighter, footX, footY, dir, breath, walkCycle, elapsedTime)
        }

        // 3. Draw Block barrier
        if (fighter.state.isBlocking) {
            val barrierColor = fighter.character.primaryColor.copy(alpha = 0.7f)
            drawScope.drawArc(
                color = barrierColor,
                startAngle = if (fighter.facingRight) -80f else 100f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(footX - (if (fighter.facingRight) 20f else 90f), footY - 180f),
                size = Size(110f, 180f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }

        // 4. Draw Hitboxes & Hurtboxes for Dojo Training mode
        if (showHitboxes) {
            val hurt = fighter.getHurtBox()
            drawScope.drawRect(
                color = Color.Green.copy(alpha = 0.35f),
                topLeft = Offset(hurt.left, groundY - hurt.top),
                size = Size(hurt.width, hurt.height),
                style = Stroke(width = 2.5f)
            )

            val hit = fighter.getHitBox()
            if (hit != null) {
                drawScope.drawRect(
                    color = Color.Red.copy(alpha = 0.55f),
                    topLeft = Offset(hit.left, groundY - hit.top),
                    size = Size(hit.width, hit.height),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }

    private fun drawSpriteCharacter(
        drawScope: DrawScope,
        fighter: FighterEntity,
        bitmap: ImageBitmap,
        footX: Float,
        footY: Float,
        dir: Float
    ) {
        val renderHeight = fighter.bodyHeight * 1.35f
        val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val renderWidth = renderHeight * aspect

        val isHurt = fighter.state == FighterState.HIT_STUN
        val isDefeated = fighter.state == FighterState.DEFEATED
        val isSuper = fighter.state == FighterState.SUPER_ART

        // Super Art Golden/Rift glowing aura behind character
        if (isSuper) {
            drawScope.drawCircle(
                color = fighter.character.primaryColor.copy(alpha = 0.45f),
                radius = renderHeight * 0.55f,
                center = Offset(footX, footY - renderHeight * 0.5f)
            )
        }

        drawScope.drawContext.canvas.save()

        // Flip horizontally if facing left
        if (dir < 0f) {
            drawScope.drawContext.transform.scale(-1f, 1f, Offset(footX, footY - renderHeight / 2f))
        }

        // Tilt if hurt or down
        if (isHurt) {
            drawScope.drawContext.transform.rotate(-10f, Offset(footX, footY))
        } else if (isDefeated) {
            drawScope.drawContext.transform.rotate(-70f, Offset(footX, footY))
        }

        // Color filter for costume palette swaps
        val tintColor = fighter.character.costume.tintColor
        val colorFilter = when {
            isHurt -> ColorFilter.tint(Color.Red.copy(alpha = 0.65f), BlendMode.SrcAtop)
            tintColor != null -> ColorFilter.tint(tintColor.copy(alpha = 0.4f), BlendMode.SrcAtop)
            else -> null
        }

        drawScope.drawImage(
            image = bitmap,
            dstOffset = IntOffset((footX - renderWidth / 2f).roundToInt(), (footY - renderHeight).roundToInt()),
            dstSize = IntSize(renderWidth.roundToInt(), renderHeight.roundToInt()),
            colorFilter = colorFilter
        )

        drawScope.drawContext.canvas.restore()
    }

    private fun drawFallbackVector(
        drawScope: DrawScope,
        f: FighterEntity,
        x: Float,
        y: Float,
        dir: Float,
        breath: Float,
        walk: Float,
        time: Float
    ) {
        val bodyH = f.bodyHeight
        val headY = y - bodyH + 30f + breath
        val baseColor = f.character.primaryColor

        // Torso
        drawScope.drawRoundRect(
            color = baseColor,
            topLeft = Offset(x - 26f, y - 160f + breath),
            size = Size(52f, 80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )

        // Head
        drawScope.drawCircle(
            color = Color(0xFFFFDFC4),
            radius = 20f,
            center = Offset(x, headY)
        )

        // Arms & Strike
        val reach = if (f.state.isAttacking) 130f else 40f
        drawScope.drawLine(
            color = baseColor,
            start = Offset(x, y - 140f + breath),
            end = Offset(x + dir * reach, y - 140f + breath),
            strokeWidth = 16f,
            cap = StrokeCap.Round
        )

        // Legs
        drawScope.drawLine(
            color = Color(0xFF1E293B),
            start = Offset(x - 12f * dir, y - 80f),
            end = Offset(x - 20f * dir + walk, y),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
        drawScope.drawLine(
            color = Color(0xFF1E293B),
            start = Offset(x + 12f * dir, y - 80f),
            end = Offset(x + 20f * dir - walk, y),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )
    }

    fun drawProjectiles(drawScope: DrawScope, projectiles: List<Projectile>, groundY: Float) {
        for (p in projectiles) {
            val cy = groundY - p.y
            val cx = p.x
            val dir = if (p.vx > 0f) 1f else -1f

            // Projectile outer glow
            drawScope.drawCircle(
                color = p.color.copy(alpha = 0.45f),
                radius = p.width * 0.7f,
                center = Offset(cx, cy)
            )

            // Inner core
            drawScope.drawOval(
                color = p.color,
                topLeft = Offset(cx - p.width / 2f, cy - p.height / 2f),
                size = Size(p.width, p.height)
            )

            // Center hot white highlight
            drawScope.drawOval(
                color = Color.White,
                topLeft = Offset(cx - (p.width * 0.5f) / 2f, cy - (p.height * 0.5f) / 2f),
                size = Size(p.width * 0.5f, p.height * 0.5f)
            )

            // Energy trailing arcs
            for (i in 1..3) {
                drawScope.drawArc(
                    color = p.color.copy(alpha = 0.5f - i * 0.12f),
                    startAngle = if (dir > 0) 120f else -60f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(cx - dir * (i * 22f) - p.width / 2f, cy - p.height / 2f),
                    size = Size(p.width, p.height),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }

    fun drawVisualEffects(drawScope: DrawScope, effects: VisualEffectsManager, groundY: Float) {
        // 1. Draw After-Images (Motion Trails)
        for (img in effects.afterImages) {
            val fy = groundY - img.y
            drawScope.drawCircle(
                color = img.color.copy(alpha = img.alpha * 0.35f),
                radius = 55f,
                center = Offset(img.x, fy - 110f)
            )
        }

        // 2. Draw Particles (Sparks, Dust, Shockwaves)
        for (p in effects.particles) {
            val py = groundY - p.y
            when (p.type) {
                ParticleType.BLOCK_CLANG -> {
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.x, py)
                    )
                }
                ParticleType.SHOCKWAVE -> {
                    drawScope.drawCircle(
                        color = p.color.copy(alpha = p.alpha * 0.7f),
                        radius = p.size * 2f,
                        center = Offset(p.x, py),
                        style = Stroke(width = 3.5f)
                    )
                }
                else -> {
                    drawScope.drawCircle(
                        color = p.color.copy(alpha = p.alpha),
                        radius = p.size,
                        center = Offset(p.x, py)
                    )
                }
            }
        }
    }
}
