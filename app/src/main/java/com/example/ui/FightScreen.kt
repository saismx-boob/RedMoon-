package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.core.*
import com.example.game.model.CharacterDef
import com.example.game.model.DummyBehavior
import com.example.game.model.GameMode
import com.example.game.renderer.FighterRenderer
import com.example.ui.theme.*
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@Composable
fun FightScreen(
    engine: FightEngine,
    onExitToMenu: () -> Unit
) {
    val snapshot by engine.snapshot.collectAsState()
    val context = LocalContext.current
    val renderer = remember(context) { FighterRenderer(context) }
    var elapsedTime by remember { mutableStateOf(0f) }

    // Run 60 FPS Game Loop
    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                lastTime = now
                elapsedTime += dt
                engine.update(dt)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- 1. Background Stage & Combat Arena Canvas ---
        Box(modifier = Modifier.fillMaxSize()) {
            // Stage Background Image
            Image(
                painter = painterResource(id = snapshot.stage.backgroundResId),
                contentDescription = snapshot.stage.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Stage Vignette Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xBB000000)
                            )
                        )
                    )
            )

            // Canvas drawing characters, particles, projectiles, and effects
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("battle_canvas")
            ) {
                val arenaW = 1440f
                val arenaH = 600f
                val scaleX = size.width / arenaW
                val scaleY = size.height / arenaH

                drawContext.transform.scale(scaleX, scaleY, Offset.Zero)

                // Screen shake offset
                val shake = engine.effects.screenShakeAmount
                if (shake > 0f) {
                    val offsetX = (kotlin.random.Random.nextFloat() - 0.5f) * shake
                    val offsetY = (kotlin.random.Random.nextFloat() - 0.5f) * shake
                    drawContext.transform.translate(offsetX, offsetY)
                }

                // Stage ambient particles (embers/rain/blossoms)
                renderer.drawStageAmbience(this, snapshot.stage, elapsedTime, arenaW, arenaH)

                // Projectiles
                renderer.drawProjectiles(this, engine.projectiles, engine.stage.floorY)

                // Draw Fighters
                renderer.drawFighter(this, engine.player, engine.stage.floorY, elapsedTime, engine.showHitboxes)
                renderer.drawFighter(this, engine.enemy, engine.stage.floorY, elapsedTime, engine.showHitboxes)

                // Visual effects (sparks, block clangs, afterimages)
                renderer.drawVisualEffects(this, engine.effects, engine.stage.floorY)
            }
        }

        // --- 2. Top Fighting HUD (Dual Health Bars, Timer, Round Pips, Meter) ---
        TopFightingHud(
            snapshot = snapshot,
            onPause = { engine.togglePause() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // --- 3. Combo Counter Badge ---
        if (snapshot.playerComboHits >= 2 && snapshot.phase == MatchPhase.FIGHTING) {
            ComboBadge(
                hits = snapshot.playerComboHits,
                damage = snapshot.playerComboDamage,
                rating = snapshot.comboRating,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 28.dp, bottom = 40.dp)
            )
        }

        // --- 4. Announcements & Overlays (Round 1, Fight, K.O., Paused) ---
        CombatOverlays(
            snapshot = snapshot,
            onRestart = { engine.restartMatch() },
            onResume = { engine.togglePause() },
            onExit = onExitToMenu,
            modifier = Modifier.align(Alignment.Center)
        )

        // --- 5. Training Mode Toolbar ---
        if (snapshot.isTraining) {
            TrainingToolbar(
                dummyBehavior = snapshot.dummyBehavior,
                showHitboxes = engine.showHitboxes,
                onToggleBehavior = {
                    val next = when (snapshot.dummyBehavior) {
                        DummyBehavior.STAND -> DummyBehavior.CROUCH
                        DummyBehavior.CROUCH -> DummyBehavior.GUARD_ALL
                        DummyBehavior.GUARD_ALL -> DummyBehavior.COUNTER_ATTACK
                        DummyBehavior.COUNTER_ATTACK -> DummyBehavior.STAND
                    }
                    engine.dummyBehavior = next
                },
                onToggleHitboxes = { engine.showHitboxes = !engine.showHitboxes },
                onRefillMeters = {
                    engine.player.meter = 100f
                    engine.enemy.meter = 100f
                    engine.player.health = engine.player.character.maxHealth
                    engine.enemy.health = engine.enemy.character.maxHealth
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp)
            )
        }

        // --- 6. Virtual Arcade Controls (D-Pad on Left, 6-Button Arcade Layout on Right) ---
        if (snapshot.phase == MatchPhase.FIGHTING) {
            VirtualArcadeControls(
                engine = engine,
                superReady = snapshot.playerMeterPercent >= 1.0f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun TopFightingHud(
    snapshot: FightSnapshot,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Player 1 Health & Meter (Left)
        FighterHudPanel(
            character = snapshot.playerCharacter,
            healthPercent = snapshot.playerHealthPercent,
            meterPercent = snapshot.playerMeterPercent,
            roundsWon = snapshot.playerRoundsWon,
            isPlayer = true,
            modifier = Modifier.weight(1f)
        )

        // Center: Timer, Round & Mode Banner
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .width(96.dp)
        ) {
            // Timer Display
            Surface(
                shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                color = Color(0xFF1E2818),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder)
            ) {
                Text(
                    text = if (snapshot.isTraining) "--" else String.format("%02d", snapshot.timeRemaining),
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    color = if (snapshot.timeRemaining <= 15 && !snapshot.isTraining) FlameCrimson else Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Text(
                text = if (snapshot.isTraining) "TRAINING" else "ROUND ${snapshot.roundNumber}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = RiftGreen,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Pause Button
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("pause_button")
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = ArcadeTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Enemy / CPU Health & Meter (Right)
        FighterHudPanel(
            character = snapshot.enemyCharacter,
            healthPercent = snapshot.enemyHealthPercent,
            meterPercent = snapshot.enemyMeterPercent,
            roundsWon = snapshot.enemyRoundsWon,
            isPlayer = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FighterHudPanel(
    character: CharacterDef,
    healthPercent: Float,
    meterPercent: Float,
    roundsWon: Int,
    isPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (isPlayer) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlayer) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
                    .border(1.5.dp, character.primaryColor, CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
            ) {
                Image(
                    painter = painterResource(id = character.portraitResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Bars & Name
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (isPlayer) Alignment.Start else Alignment.End
        ) {
            // Name row & Round pips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isPlayer) Arrangement.SpaceBetween else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlayer) {
                    Text(
                        text = character.name.uppercase(),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    RoundPips(wins = roundsWon, color = character.primaryColor)
                } else {
                    RoundPips(wins = roundsWon, color = character.primaryColor)
                    Text(
                        text = character.name.uppercase(),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Health Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color(0xFF141A12))
                    .border(1.dp, Color(0xFF384533))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(healthPercent)
                        .align(if (isPlayer) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(
                            Brush.horizontalGradient(
                                if (isPlayer) listOf(RiftGreen, Color(0xFFA5D64C))
                                else listOf(FlameOrange, FlameCrimson)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // EX Super Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isPlayer) Arrangement.SpaceBetween else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (meterPercent >= 1.0f) "SUPER READY!" else "EX METER",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (meterPercent >= 1.0f) ArcadeGold else ArcadeTextMuted,
                    letterSpacing = 1.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFF161E14))
                    .border(1.dp, if (meterPercent >= 1.0f) ArcadeGold else Color(0xFF2C3925))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(meterPercent)
                        .align(if (isPlayer) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(
                            if (meterPercent >= 1.0f) Brush.horizontalGradient(listOf(ArcadeGold, Color(0xFFF59E0B)))
                            else Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))
                        )
                )
            }
        }

        if (!isPlayer) {
            Spacer(modifier = Modifier.width(8.dp))
            // Enemy Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
                    .border(1.5.dp, character.primaryColor, CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
            ) {
                Image(
                    painter = painterResource(id = character.portraitResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun RoundPips(wins: Int, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (index < wins) color else Color(0xFF293524))
                    .border(1.dp, color.copy(alpha = 0.6f), CircleShape)
            )
        }
    }
}

@Composable
private fun ComboBadge(
    hits: Int,
    damage: Float,
    rating: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
        color = Color(0xE6131A10),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RiftGreen),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$hits",
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = RiftGreen,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "HITS!",
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                text = rating,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = ArcadeGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "${damage.toInt()} TOTAL DAMAGE",
                fontSize = 9.sp,
                color = ArcadeTextMuted,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CombatOverlays(
    snapshot: FightSnapshot,
    onRestart: () -> Unit,
    onResume: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        snapshot.phase == MatchPhase.COUNTDOWN && snapshot.announcementBanner.isNotEmpty() -> {
            Text(
                text = snapshot.announcementBanner,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black,
                fontSize = 62.sp,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = modifier
            )
        }

        snapshot.phase == MatchPhase.ROUND_OVER -> {
            Column(
                modifier = modifier
                    .background(Color(0xD90D120B))
                    .padding(horizontal = 36.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = snapshot.announcementBanner,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = if (snapshot.winnerIsPlayer == true) RiftGreen else FlameCrimson,
                    letterSpacing = 2.sp
                )
            }
        }

        snapshot.phase == MatchPhase.MATCH_OVER -> {
            Surface(
                shape = CutCornerShape(topStart = 0.dp, bottomEnd = 16.dp),
                color = Color(0xF2121A10),
                border = androidx.compose.foundation.BorderStroke(2.dp, RiftGreen),
                modifier = modifier.widthIn(max = 440.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (snapshot.winnerIsPlayer == true) "VICTORY!" else "DEFEATED",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 44.sp,
                        color = if (snapshot.winnerIsPlayer == true) RiftGreen else FlameCrimson,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (snapshot.winnerIsPlayer == true) snapshot.playerCharacter.quoteWin else snapshot.playerCharacter.quoteLoss,
                        color = ArcadeTextLight,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )

                    if (snapshot.gameMode == GameMode.SURVIVAL) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SURVIVAL STREAK: ${snapshot.survivalWins} WINS",
                            color = ArcadeGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Button(
                            onClick = onRestart,
                            colors = ButtonDefaults.buttonColors(containerColor = RiftGreen, contentColor = Color.Black),
                            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                            modifier = Modifier.testTag("fight_again_button")
                        ) {
                            Text("FIGHT AGAIN", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onExit,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                            modifier = Modifier.testTag("exit_to_title_button")
                        ) {
                            Text("EXIT TO MENU")
                        }
                    }
                }
            }
        }

        snapshot.phase == MatchPhase.PAUSED -> {
            Surface(
                shape = CutCornerShape(topStart = 0.dp, bottomEnd = 16.dp),
                color = Color(0xF2121A10),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ArcadeBorder),
                modifier = modifier.widthIn(max = 380.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PAUSED",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = RiftGreen, contentColor = Color.Black),
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resume_fight_button")
                    ) {
                        Text("RESUME BATTLE", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRestart,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RESTART ROUND")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onExit,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("QUIT TO MENU")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingToolbar(
    dummyBehavior: DummyBehavior,
    showHitboxes: Boolean,
    onToggleBehavior: () -> Unit,
    onToggleHitboxes: () -> Unit,
    onRefillMeters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xDD172013),
        border = androidx.compose.foundation.BorderStroke(1.dp, RiftGreen.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onToggleBehavior,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384533)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RiftGreen),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("training_dummy_behavior")
            ) {
                Text("DUMMY: ${dummyBehavior.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onToggleHitboxes,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384533)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (showHitboxes) RiftGreen else ArcadeTextMuted
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("training_toggle_hitboxes")
            ) {
                Text(if (showHitboxes) "HITBOXES: ON" else "HITBOXES: OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onRefillMeters,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF384533)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcadeGold),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.testTag("training_refill_meters")
            ) {
                Text("REFILL METERS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VirtualArcadeControls(
    engine: FightEngine,
    superReady: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // --- Left: Virtual 8-way Arcade Joystick / Directional Pad ---
        VirtualDpad(
            onDirection = { dir -> engine.onPlayerDirection(dir) },
            onDashForward = { engine.onPlayerDash(forward = true) },
            onDashBack = { engine.onPlayerDash(forward = false) }
        )

        // --- Right: 6-Button Arcade Fight Pad (LP, HP, LK, HK, SP, EX SUPER) + BLOCK ---
        VirtualFightButtons(
            onAttack = { type -> engine.onPlayerAttack(type) },
            onBlock = { isDown -> engine.onPlayerBlock(isDown) },
            superReady = superReady
        )
    }
}

@Composable
private fun VirtualDpad(
    onDirection: (Direction) -> Unit,
    onDashForward: () -> Unit,
    onDashBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        // Up / Jump
        ArcadeDirButton(
            icon = Icons.Default.ArrowUpward,
            label = "JUMP",
            tag = "btn_jump",
            onDown = { onDirection(Direction.UP) },
            onUp = { onDirection(Direction.NEUTRAL) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Left
            ArcadeDirButton(
                icon = Icons.Default.ArrowBack,
                label = "LEFT",
                tag = "btn_left",
                onDown = { onDirection(Direction.LEFT) },
                onUp = { onDirection(Direction.NEUTRAL) }
            )

            // Crouch / Down
            ArcadeDirButton(
                icon = Icons.Default.ArrowDownward,
                label = "CROUCH",
                tag = "btn_crouch",
                onDown = { onDirection(Direction.DOWN) },
                onUp = { onDirection(Direction.NEUTRAL) }
            )

            // Right
            ArcadeDirButton(
                icon = Icons.Default.ArrowForward,
                label = "RIGHT",
                tag = "btn_right",
                onDown = { onDirection(Direction.RIGHT) },
                onUp = { onDirection(Direction.NEUTRAL) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick Dash buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                onClick = onDashBack,
                shape = RoundedCornerShape(4.dp),
                color = Color(0xBB1D2719),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32402A)),
                modifier = Modifier.testTag("btn_dash_back")
            ) {
                Text("DASH <<", color = ArcadeTextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }

            Surface(
                onClick = onDashForward,
                shape = RoundedCornerShape(4.dp),
                color = Color(0xBB1D2719),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32402A)),
                modifier = Modifier.testTag("btn_dash_fwd")
            ) {
                Text(">> DASH", color = ArcadeTextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun ArcadeDirButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
        color = if (isPressed) Color(0xFF3E5032) else Color(0xD91F2B1B),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPressed) RiftGreen else Color(0xFF3D4E33)),
        modifier = Modifier
            .size(46.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true; onDown() },
                    onDragEnd = { isPressed = false; onUp() },
                    onDragCancel = { isPressed = false; onUp() },
                    onDrag = { _, _ -> }
                )
            }
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isPressed) RiftGreen else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VirtualFightButtons(
    onAttack: (AttackType) -> Unit,
    onBlock: (Boolean) -> Unit,
    superReady: Boolean
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        // Top Row: LP, HP, SP (Special)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcadeActionButton(
                label = "LP",
                sub = "PUNCH",
                accent = Color(0xFF93C5FD),
                tag = "btn_lp",
                onClick = { onAttack(AttackType.LIGHT_PUNCH) }
            )

            ArcadeActionButton(
                label = "HP",
                sub = "SLASH",
                accent = Color(0xFF3B82F6),
                tag = "btn_hp",
                onClick = { onAttack(AttackType.HEAVY_PUNCH) }
            )

            ArcadeActionButton(
                label = "SP",
                sub = "SPECIAL",
                accent = RiftGreen,
                tag = "btn_sp",
                onClick = { onAttack(AttackType.SPECIAL) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Row: LK, HK, EX SUPER + GUARD
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcadeActionButton(
                label = "LK",
                sub = "KICK",
                accent = Color(0xFFFCA5A5),
                tag = "btn_lk",
                onClick = { onAttack(AttackType.LIGHT_KICK) }
            )

            ArcadeActionButton(
                label = "HK",
                sub = "LAUNCH",
                accent = FlameCrimson,
                tag = "btn_hk",
                onClick = { onAttack(AttackType.HEAVY_KICK) }
            )

            // Super Art button (Glows gold when 100% meter ready!)
            ArcadeActionButton(
                label = "SUPER",
                sub = if (superReady) "READY!" else "100%",
                accent = if (superReady) ArcadeGold else Color.Gray,
                isSuper = superReady,
                tag = "btn_super",
                onClick = { onAttack(AttackType.SUPER) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Guard Button
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            var isGuardActive by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isGuardActive) Color(0xFF3E5032) else Color(0xD91F2B1B),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isGuardActive) Color.White else Color(0xFF435837)),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isGuardActive = true; onBlock(true) },
                            onDragEnd = { isGuardActive = false; onBlock(false) },
                            onDragCancel = { isGuardActive = false; onBlock(false) },
                            onDrag = { _, _ -> }
                        )
                    }
                    .testTag("btn_guard")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GUARD / BLOCK",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcadeActionButton(
    label: String,
    sub: String,
    accent: Color,
    tag: String,
    isSuper: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
        color = if (isSuper) Color(0xFF33270A) else Color(0xD91D2619),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accent),
        modifier = Modifier
            .size(width = 52.dp, height = 48.dp)
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = sub,
                color = ArcadeTextMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
