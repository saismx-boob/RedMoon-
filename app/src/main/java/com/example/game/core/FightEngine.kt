package com.example.game.core

import androidx.compose.ui.graphics.Color
import com.example.game.content.FighterRegistry
import com.example.game.content.StageRegistry
import com.example.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

enum class MatchPhase {
    INTRO,
    COUNTDOWN,
    FIGHTING,
    HIT_FREEZE,
    ROUND_OVER,
    MATCH_OVER,
    PAUSED
}

data class FightSnapshot(
    val phase: MatchPhase,
    val roundNumber: Int,
    val timeRemaining: Int,
    val playerHealthPercent: Float,
    val enemyHealthPercent: Float,
    val playerMeterPercent: Float,
    val enemyMeterPercent: Float,
    val playerRoundsWon: Int,
    val enemyRoundsWon: Int,
    val playerCharacter: CharacterDef,
    val enemyCharacter: CharacterDef,
    val stage: StageDef,
    val gameMode: GameMode,
    val playerComboHits: Int,
    val playerComboDamage: Float,
    val comboRating: String,
    val announcementBanner: String,
    val winnerIsPlayer: Boolean?,
    val survivalWins: Int = 0,
    val isTraining: Boolean = false,
    val dummyBehavior: DummyBehavior = DummyBehavior.STAND
)

class FightEngine(
    val playerCharacter: CharacterDef,
    val enemyCharacter: CharacterDef,
    val stage: StageDef,
    val gameMode: GameMode,
    val aiDifficulty: AiDifficulty,
    val audio: AudioSynthesizer,
    val haptic: HapticManager?
) {
    val player = FighterEntity(playerCharacter, isPlayer = true, startX = 360f)
    val enemy = FighterEntity(enemyCharacter, isPlayer = false, startX = 1080f)

    val effects = VisualEffectsManager()
    val comboTracker = ComboTracker()
    val ai = FighterAI(aiDifficulty)

    val projectiles = mutableListOf<Projectile>()
    private var projectileIdCounter = 0L

    var matchPhase: MatchPhase = MatchPhase.COUNTDOWN
    var roundNumber: Int = 1
    var timeRemaining: Float = 99f
    var countdownTimer: Float = 2.0f
    var roundOverTimer: Float = 2.5f
    var winnerIsPlayer: Boolean? = null
    var bannerText: String = "ROUND 1"
    var survivalWins: Int = 0

    // Training mode dummy behavior
    var dummyBehavior: DummyBehavior = DummyBehavior.STAND
    var showHitboxes: Boolean = false

    private val arenaWidth = 1440f
    private val arenaFloorY = stage.floorY

    private val _snapshot = MutableStateFlow(createSnapshot())
    val snapshot: StateFlow<FightSnapshot> = _snapshot.asStateFlow()

    init {
        audio.playSound("round_start")
    }

    fun update(dt: Float) {
        if (matchPhase == MatchPhase.PAUSED) return

        effects.update(dt)
        comboTracker.update(dt)

        when (matchPhase) {
            MatchPhase.COUNTDOWN -> {
                countdownTimer -= dt
                if (countdownTimer > 0.8f) {
                    bannerText = "ROUND $roundNumber"
                } else if (countdownTimer > 0f) {
                    bannerText = "FIGHT!"
                } else {
                    matchPhase = MatchPhase.FIGHTING
                    bannerText = ""
                }
            }

            MatchPhase.FIGHTING -> {
                if (gameMode != GameMode.TRAINING) {
                    timeRemaining = (timeRemaining - dt).coerceAtLeast(0f)
                }

                // Check Hit-Stop freeze
                if (effects.hitStopFrames > 0) {
                    effects.hitStopFrames--
                    emitSnapshot()
                    return
                }

                // AI Decisions
                if (gameMode == GameMode.TRAINING) {
                    updateTrainingDummy(dt)
                } else {
                    ai.decide(enemy, player, dt) { command ->
                        handleAiCommand(command)
                    }
                }

                // Update entities
                updateFighter(player, dt)
                updateFighter(enemy, dt)

                // Face each other
                if (!player.state.isAttacking && player.state != FighterState.KNOCKDOWN) {
                    player.facingRight = player.x <= enemy.x
                }
                if (!enemy.state.isAttacking && enemy.state != FighterState.KNOCKDOWN) {
                    enemy.facingRight = enemy.x < player.x
                }

                // Pushbox physical separation
                resolvePushboxCollision()

                // Check attack collisions
                checkAttackCollisions()

                // Update projectiles
                updateProjectiles(dt)

                // Check Round End Conditions
                if (gameMode != GameMode.TRAINING) {
                    if (player.health <= 0f || enemy.health <= 0f || timeRemaining <= 0f) {
                        endRound()
                    }
                } else {
                    // In training mode, auto-refill if depleted
                    if (player.health <= 0f) player.health = player.character.maxHealth
                    if (enemy.health <= 0f) {
                        enemy.health = enemy.character.maxHealth
                        enemy.state = FighterState.IDLE
                    }
                }
            }

            MatchPhase.ROUND_OVER -> {
                roundOverTimer -= dt
                updateFighter(player, dt * 0.4f) // Slow motion on KO
                updateFighter(enemy, dt * 0.4f)
                if (roundOverTimer <= 0f) {
                    proceedAfterRound()
                }
            }

            else -> {}
        }

        emitSnapshot()
    }

    private fun updateFighter(f: FighterEntity, dt: Float) {
        f.stateTime += dt

        // 1. Advance attack animation
        if (f.state.isAttacking) {
            val attack = f.currentAttack
            if (attack != null) {
                f.attackFrameIndex = (f.stateTime * 60f).toInt()
                if (f.attackFrameIndex >= attack.totalFrames) {
                    f.state = if (f.y > 0f) FighterState.JUMP_UP else FighterState.IDLE
                    f.currentAttack = null
                    f.attackFrameIndex = 0
                    f.hitConfirmed = false
                    f.isCounterHitState = false
                }
            }
        }

        // 2. Advance hit/block stun
        if (f.state == FighterState.HIT_STUN) {
            f.hitStunRemaining--
            if (f.hitStunRemaining <= 0) {
                f.state = if (f.y > 0f) FighterState.JUMP_UP else FighterState.IDLE
            }
        } else if (f.state == FighterState.BLOCK_STUN) {
            f.blockStunRemaining--
            if (f.blockStunRemaining <= 0) {
                f.state = FighterState.IDLE
            }
        } else if (f.state == FighterState.KNOCKDOWN) {
            if (f.stateTime > 0.8f) {
                f.state = FighterState.WAKEUP
                f.stateTime = 0f
            }
        } else if (f.state == FighterState.WAKEUP) {
            if (f.stateTime > 0.3f) {
                f.state = FighterState.IDLE
                f.stateTime = 0f
            }
        }

        // 3. Physics & Jump / Gravity
        if (f.y > 0f || f.vy > 0f) {
            f.y += f.vy * dt
            f.vy -= f.character.gravity * dt
            if (f.y <= 0f) {
                f.y = 0f
                f.vy = 0f
                f.isAirborne = false
                effects.spawnDust(f.x, arenaFloorY, 8)
                if (f.state in listOf(FighterState.JUMP_UP, FighterState.JUMP_FORWARD, FighterState.JUMP_BACK)) {
                    f.state = FighterState.IDLE
                }
            }
        }

        // 4. Horizontal movement & friction
        f.x += f.vx * dt
        f.vx *= Math.pow(0.02, dt.toDouble()).toFloat()

        // Clamp to screen arena boundaries
        val padding = 75f
        if (f.x < padding) {
            f.x = padding
            f.vx = 0f
        } else if (f.x > arenaWidth - padding) {
            f.x = arenaWidth - padding
            f.vx = 0f
        }

        // Passive slight meter recharge
        f.meter = (f.meter + dt * 2.5f).coerceAtMost(100f)
    }

    private fun resolvePushboxCollision() {
        val dist = abs(player.x - enemy.x)
        val minDist = (player.bodyWidth + enemy.bodyWidth) * 0.42f
        if (dist < minDist && abs(player.y - enemy.y) < 140f) {
            val overlap = (minDist - dist) / 2f
            if (player.x <= enemy.x) {
                player.x = (player.x - overlap).coerceAtLeast(75f)
                enemy.x = (enemy.x + overlap).coerceAtMost(arenaWidth - 75f)
            } else {
                player.x = (player.x + overlap).coerceAtMost(arenaWidth - 75f)
                enemy.x = (enemy.x - overlap).coerceAtLeast(75f)
            }
        }
    }

    private fun checkAttackCollisions() {
        checkSingleHit(player, enemy)
        checkSingleHit(enemy, player)
    }

    private fun checkSingleHit(attacker: FighterEntity, defender: FighterEntity) {
        val attack = attacker.currentAttack ?: return
        val hitbox = attacker.getHitBox() ?: return
        val hurtbox = defender.getHurtBox()

        if (hitbox.intersects(hurtbox)) {
            attacker.hitConfirmed = true

            val isBlocking = defender.state.isBlocking ||
                    (defender.state == FighterState.WALK_BACK && !defender.isAirborne)

            val dir = if (attacker.facingRight) 1f else -1f
            val hitX = (hitbox.centerX + hurtbox.centerX) / 2f
            val hitY = (hitbox.centerY + hurtbox.centerY) / 2f

            if (isBlocking) {
                // Blocked hit
                defender.state = if (attack.isLow) FighterState.BLOCK_CROUCH else FighterState.BLOCK_STAND
                defender.blockStunRemaining = attack.blockStunFrames
                val chipDamage = (attack.damage * 0.12f).coerceAtLeast(1f)
                defender.health = (defender.health - chipDamage).coerceAtLeast(1f)
                defender.vx = dir * (attack.pushback * 0.6f)

                attacker.meter = (attacker.meter + attack.meterGain * 0.5f).coerceAtMost(100f)
                defender.meter = (defender.meter + 6f).coerceAtMost(100f)

                effects.spawnBlockSparks(hitX, hitY)
                effects.addFloatingText(hitX, hitY, "GUARD", Color(0xFFE2E8F0))
                audio.playSound("block")
                haptic?.onBlock()
            } else {
                // Clean Hit / Counter Hit
                val isCounter = defender.isCounterHitState
                val isAirHit = defender.y > 0f
                var finalDmg = attack.damage
                if (isCounter) finalDmg *= 1.25f

                // Register combo damage
                if (attacker.isPlayer) {
                    finalDmg = comboTracker.registerHit(finalDmg, isAirHit)
                }

                defender.health = (defender.health - finalDmg).coerceAtLeast(0f)
                attacker.meter = (attacker.meter + attack.meterGain).coerceAtMost(100f)
                defender.meter = (defender.meter + 12f).coerceAtMost(100f)

                // Hit Stop freeze (Street Fighter style)
                effects.hitStopFrames = when (attack.type) {
                    AttackType.SUPER -> 10
                    AttackType.SPECIAL, AttackType.HEAVY_PUNCH, AttackType.HEAVY_KICK -> 6
                    else -> 3
                }

                if (attack.isLauncher || attack.isKnockdown || isAirHit) {
                    defender.state = FighterState.KNOCKDOWN
                    defender.vy = if (attack.isLauncher) 620f else 340f
                    defender.vx = dir * (attack.pushback * 0.9f)
                    defender.y = 1f
                } else {
                    defender.state = FighterState.HIT_STUN
                    defender.hitStunRemaining = attack.hitStunFrames + (if (isCounter) 4 else 0)
                    defender.vx = dir * attack.pushback
                }

                effects.spawnHitSparks(
                    hitX, hitY,
                    count = if (attack.type == AttackType.SUPER) 24 else 12,
                    baseColor = attacker.character.primaryColor,
                    isHeavy = attack.type != AttackType.LIGHT_PUNCH && attack.type != AttackType.LIGHT_KICK
                )

                effects.addFloatingText(
                    hitX, hitY,
                    text = if (isCounter) "COUNTER! ${finalDmg.toInt()}" else "${finalDmg.toInt()}",
                    color = if (isCounter) Color(0xFFFACC15) else attacker.character.primaryColor,
                    isCrit = isCounter
                )

                audio.playSound(attack.soundKey)
                if (attack.type == AttackType.SUPER) {
                    haptic?.onSuperArt()
                } else if (attack.type in listOf(AttackType.HEAVY_PUNCH, AttackType.HEAVY_KICK, AttackType.SPECIAL)) {
                    haptic?.onHeavyHit()
                } else {
                    haptic?.onLightHit()
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.life -= dt

            val target = if (p.isPlayer) enemy else player
            val targetHurt = target.getHurtBox()
            val projBox = BoundingBox(
                p.x - p.width / 2f,
                p.y + p.height / 2f,
                p.x + p.width / 2f,
                p.y - p.height / 2f
            )

            if (projBox.intersects(targetHurt)) {
                val isBlocking = target.state.isBlocking
                val dir = if (p.vx > 0f) 1f else -1f
                val dmg = if (isBlocking) (p.damage * 0.15f).coerceAtLeast(1f) else p.damage

                target.health = (target.health - dmg).coerceAtLeast(0f)
                target.vx = dir * (if (isBlocking) 40f else 85f)
                target.state = if (isBlocking) FighterState.BLOCK_STAND else FighterState.HIT_STUN
                target.hitStunRemaining = 18

                effects.spawnHitSparks(p.x, p.y, 16, p.color, isHeavy = true)
                audio.playSound("hit_heavy")
                iter.remove()
                continue
            }

            if (p.life <= 0f || p.x < -100f || p.x > arenaWidth + 100f) {
                iter.remove()
            }
        }
    }

    private fun spawnProjectile(attacker: FighterEntity, move: MoveData) {
        val dir = if (attacker.facingRight) 1f else -1f
        val projX = attacker.x + dir * 110f
        val projY = attacker.y + move.heightOffset
        val speed = dir * 720f

        projectiles.add(
            Projectile(
                id = ++projectileIdCounter,
                x = projX,
                y = projY,
                vx = speed,
                width = 80f,
                height = 55f,
                damage = move.damage,
                isPlayer = attacker.isPlayer,
                color = attacker.character.primaryColor,
                effectType = move.name
            )
        )
        effects.spawnDust(attacker.x, arenaFloorY, 5)
    }

    // --- User Player Actions ---
    fun onPlayerDirection(dir: Direction) {
        if (matchPhase != MatchPhase.FIGHTING || player.state.isHitOrDown) return

        when (dir) {
            Direction.LEFT -> {
                if (player.facingRight) {
                    player.vx = -player.character.walkSpeed
                    player.state = FighterState.WALK_BACK
                } else {
                    player.vx = -player.character.walkSpeed
                    player.state = FighterState.WALK_FORWARD
                }
            }
            Direction.RIGHT -> {
                if (player.facingRight) {
                    player.vx = player.character.walkSpeed
                    player.state = FighterState.WALK_FORWARD
                } else {
                    player.vx = player.character.walkSpeed
                    player.state = FighterState.WALK_BACK
                }
            }
            Direction.UP -> {
                if (player.y <= 0f) {
                    player.vy = player.character.jumpVelocity
                    player.y = 1f
                    player.state = FighterState.JUMP_UP
                    player.isAirborne = true
                    effects.spawnDust(player.x, arenaFloorY, 6)
                    audio.playSound("jump")
                }
            }
            Direction.UP_LEFT -> {
                if (player.y <= 0f) {
                    player.vy = player.character.jumpVelocity
                    player.vx = -player.character.walkSpeed * 0.9f
                    player.y = 1f
                    player.state = FighterState.JUMP_BACK
                    player.isAirborne = true
                    effects.spawnDust(player.x, arenaFloorY, 6)
                    audio.playSound("jump")
                }
            }
            Direction.UP_RIGHT -> {
                if (player.y <= 0f) {
                    player.vy = player.character.jumpVelocity
                    player.vx = player.character.walkSpeed * 0.9f
                    player.y = 1f
                    player.state = FighterState.JUMP_FORWARD
                    player.isAirborne = true
                    effects.spawnDust(player.x, arenaFloorY, 6)
                    audio.playSound("jump")
                }
            }
            Direction.DOWN, Direction.DOWN_LEFT, Direction.DOWN_RIGHT -> {
                if (player.y <= 0f) {
                    player.state = FighterState.CROUCH
                    player.vx = 0f
                }
            }
            Direction.NEUTRAL -> {
                if (player.y <= 0f && player.state.canMove) {
                    player.state = FighterState.IDLE
                    player.vx = 0f
                }
            }
        }
    }

    fun onPlayerDash(forward: Boolean) {
        if (matchPhase != MatchPhase.FIGHTING || player.state.isHitOrDown || player.y > 0f) return
        val dir = if (forward == player.facingRight) 1f else -1f
        player.vx = dir * player.character.dashSpeed
        player.state = if (forward) FighterState.DASH_FORWARD else FighterState.DASH_BACK
        effects.addAfterImage(player.x, player.y, player.facingRight, player.character.primaryColor, player.state)
        effects.spawnDust(player.x, arenaFloorY, 8)
        audio.playSound("dash")
    }

    fun onPlayerAttack(type: AttackType) {
        if (matchPhase != MatchPhase.FIGHTING || player.state.isHitOrDown) return

        val move = when (type) {
            AttackType.LIGHT_PUNCH -> player.character.lightPunch
            AttackType.HEAVY_PUNCH -> player.character.heavyPunch
            AttackType.LIGHT_KICK -> player.character.lightKick
            AttackType.HEAVY_KICK -> player.character.heavyKick
            AttackType.SPECIAL -> player.character.specialMove.moveData
            AttackType.SUPER -> player.character.superArt.moveData
        }

        // Check if canceling from current attack
        if (player.state.isAttacking) {
            val curr = player.currentAttack ?: return
            val canCancelSpecial = curr.canCancelIntoSpecial && type == AttackType.SPECIAL && player.hitConfirmed
            val canCancelSuper = curr.canCancelIntoSuper && type == AttackType.SUPER && player.hitConfirmed
            if (!canCancelSpecial && !canCancelSuper) return
        }

        val started = player.startAttack(move)
        if (started) {
            if (move.spawnsProjectile) {
                spawnProjectile(player, move)
            }
            if (type == AttackType.SUPER) {
                effects.spawnSuperArtFlash(player.x, player.y + 130f, player.character.primaryColor)
                audio.playSound("super_activation")
                haptic?.onSuperArt()
            }
        }
    }

    fun onPlayerBlock(isDown: Boolean) {
        if (matchPhase != MatchPhase.FIGHTING || player.state.isHitOrDown) return
        if (isDown) {
            player.state = FighterState.BLOCK_STAND
            player.vx = 0f
        } else if (player.state.isBlocking) {
            player.state = FighterState.IDLE
        }
    }

    // --- AI Execution ---
    private fun handleAiCommand(cmd: AiCommand) {
        when (cmd) {
            AiCommand.WALK_FORWARD -> {
                enemy.vx = (if (enemy.facingRight) 1f else -1f) * enemy.character.walkSpeed
                enemy.state = FighterState.WALK_FORWARD
            }
            AiCommand.WALK_BACK -> {
                enemy.vx = (if (enemy.facingRight) -1f else 1f) * enemy.character.walkSpeed
                enemy.state = FighterState.WALK_BACK
            }
            AiCommand.DASH_FORWARD -> {
                enemy.vx = (if (enemy.facingRight) 1f else -1f) * enemy.character.dashSpeed
                enemy.state = FighterState.DASH_FORWARD
                effects.addAfterImage(enemy.x, enemy.y, enemy.facingRight, enemy.character.primaryColor, enemy.state)
            }
            AiCommand.DASH_BACK -> {
                enemy.vx = (if (enemy.facingRight) -1f else 1f) * enemy.character.dashSpeed
                enemy.state = FighterState.DASH_BACK
            }
            AiCommand.JUMP -> {
                if (enemy.y <= 0f) {
                    enemy.vy = enemy.character.jumpVelocity
                    enemy.y = 1f
                    enemy.state = FighterState.JUMP_UP
                }
            }
            AiCommand.CROUCH -> {
                enemy.state = FighterState.CROUCH
                enemy.vx = 0f
            }
            AiCommand.LIGHT_PUNCH -> enemy.startAttack(enemy.character.lightPunch)
            AiCommand.HEAVY_PUNCH -> enemy.startAttack(enemy.character.heavyPunch)
            AiCommand.LIGHT_KICK -> enemy.startAttack(enemy.character.lightKick)
            AiCommand.HEAVY_KICK -> enemy.startAttack(enemy.character.heavyKick)
            AiCommand.SPECIAL_MOVE -> {
                if (enemy.startAttack(enemy.character.specialMove.moveData)) {
                    spawnProjectile(enemy, enemy.character.specialMove.moveData)
                }
            }
            AiCommand.SUPER_ART -> {
                if (enemy.startAttack(enemy.character.superArt.moveData)) {
                    effects.spawnSuperArtFlash(enemy.x, enemy.y + 130f, enemy.character.primaryColor)
                    audio.playSound("super_activation")
                }
            }
            AiCommand.BLOCK_HIGH -> {
                enemy.state = FighterState.BLOCK_STAND
                enemy.vx = 0f
            }
            AiCommand.BLOCK_LOW -> {
                enemy.state = FighterState.BLOCK_CROUCH
                enemy.vx = 0f
            }
        }
    }

    private fun updateTrainingDummy(dt: Float) {
        when (dummyBehavior) {
            DummyBehavior.STAND -> {
                if (!enemy.state.isHitOrDown) enemy.state = FighterState.IDLE
            }
            DummyBehavior.CROUCH -> {
                if (!enemy.state.isHitOrDown) enemy.state = FighterState.CROUCH
            }
            DummyBehavior.GUARD_ALL -> {
                if (!enemy.state.isHitOrDown) enemy.state = FighterState.BLOCK_STAND
            }
            DummyBehavior.COUNTER_ATTACK -> {
                if (!enemy.state.isHitOrDown && enemy.stateTime > 0.4f) {
                    enemy.startAttack(enemy.character.lightPunch)
                }
            }
        }
    }

    private fun endRound() {
        matchPhase = MatchPhase.ROUND_OVER
        roundOverTimer = 2.4f
        audio.playSound("ko")

        winnerIsPlayer = when {
            player.health > enemy.health -> true
            enemy.health > player.health -> false
            else -> null // Draw
        }

        if (winnerIsPlayer == true) {
            player.roundsWon++
            enemy.state = FighterState.DEFEATED
            player.state = FighterState.VICTORY
            bannerText = "K.O. - ${player.character.name.uppercase()} WINS!"
        } else if (winnerIsPlayer == false) {
            enemy.roundsWon++
            player.state = FighterState.DEFEATED
            enemy.state = FighterState.VICTORY
            bannerText = "K.O. - ${enemy.character.name.uppercase()} WINS!"
        } else {
            bannerText = "DOUBLE K.O.!"
        }
    }

    private fun proceedAfterRound() {
        val matchFinished = (player.roundsWon >= 2 || enemy.roundsWon >= 2) && gameMode != GameMode.TRAINING
        if (matchFinished) {
            matchPhase = MatchPhase.MATCH_OVER
            if (winnerIsPlayer == true && gameMode == GameMode.SURVIVAL) {
                survivalWins++
            }
        } else {
            // Next round
            roundNumber++
            player.resetForRound(startX = 360f, initialFacingRight = true)
            enemy.resetForRound(startX = 1080f, initialFacingRight = false)
            projectiles.clear()
            effects.clear()
            comboTracker.reset()
            timeRemaining = 99f
            countdownTimer = 2.0f
            matchPhase = MatchPhase.COUNTDOWN
            audio.playSound("round_start")
        }
    }

    fun restartMatch() {
        roundNumber = 1
        player.roundsWon = 0
        enemy.roundsWon = 0
        player.resetForRound(startX = 360f, initialFacingRight = true)
        enemy.resetForRound(startX = 1080f, initialFacingRight = false)
        projectiles.clear()
        effects.clear()
        comboTracker.reset()
        timeRemaining = 99f
        countdownTimer = 2.0f
        matchPhase = MatchPhase.COUNTDOWN
        winnerIsPlayer = null
        audio.playSound("round_start")
    }

    fun togglePause() {
        matchPhase = if (matchPhase == MatchPhase.PAUSED) MatchPhase.FIGHTING else MatchPhase.PAUSED
    }

    private fun createSnapshot(): FightSnapshot {
        return FightSnapshot(
            phase = matchPhase,
            roundNumber = roundNumber,
            timeRemaining = timeRemaining.toInt(),
            playerHealthPercent = (player.health / player.character.maxHealth).coerceIn(0f, 1f),
            enemyHealthPercent = (enemy.health / enemy.character.maxHealth).coerceIn(0f, 1f),
            playerMeterPercent = (player.meter / 100f).coerceIn(0f, 1f),
            enemyMeterPercent = (enemy.meter / 100f).coerceIn(0f, 1f),
            playerRoundsWon = player.roundsWon,
            enemyRoundsWon = enemy.roundsWon,
            playerCharacter = player.character,
            enemyCharacter = enemy.character,
            stage = stage,
            gameMode = gameMode,
            playerComboHits = comboTracker.hitCount,
            playerComboDamage = comboTracker.totalDamage,
            comboRating = comboTracker.ratingText,
            announcementBanner = bannerText,
            winnerIsPlayer = winnerIsPlayer,
            survivalWins = survivalWins,
            isTraining = gameMode == GameMode.TRAINING,
            dummyBehavior = dummyBehavior
        )
    }

    private fun emitSnapshot() {
        _snapshot.value = createSnapshot()
    }
}
