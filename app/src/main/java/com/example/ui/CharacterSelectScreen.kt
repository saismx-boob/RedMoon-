package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.content.FighterRegistry
import com.example.game.content.StageRegistry
import com.example.game.model.CharacterDef
import com.example.game.model.GameMode
import com.example.game.model.StageDef
import com.example.ui.theme.*

@Composable
fun CharacterSelectScreen(
    gameMode: GameMode,
    onBack: () -> Unit,
    onStartBattle: (playerChar: CharacterDef, enemyChar: CharacterDef, stage: StageDef) -> Unit
) {
    var selectedPlayer by remember { mutableStateOf(FighterRegistry.KAIRO) }
    var selectedEnemy by remember { mutableStateOf(FighterRegistry.COLOSSUS) }
    var selectedStage by remember { mutableStateOf(StageRegistry.ASHEN_RUINS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("char_select_back")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RiftGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CHOOSE YOUR FIGHTER",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MODE: ${gameMode.name} • SELECT P1 & OPPONENT",
                            fontSize = 10.sp,
                            color = ArcadeTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stage Picker dropdown / toggle
                StageMiniPicker(
                    selectedStage = selectedStage,
                    onSelectStage = { selectedStage = it }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Area: Fighter Slots (P1 on Left, Grid in Center, P2 on Right)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pane: Player 1 Preview Card
                FighterPreviewCard(
                    title = "PLAYER 1",
                    character = selectedPlayer,
                    isPlayerOne = true,
                    modifier = Modifier.weight(0.9f)
                )

                // Center Pane: Character Roster Grid
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "FIGHTER ROSTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcadeTextMuted,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FighterRegistry.ALL_FIGHTERS.forEach { fighter ->
                            val isP1 = selectedPlayer.id == fighter.id
                            val isP2 = selectedEnemy.id == fighter.id

                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
                                    .background(Color(0xFF1E2818))
                                    .border(
                                        width = if (isP1 || isP2) 2.5.dp else 1.dp,
                                        color = when {
                                            isP1 -> RiftGreen
                                            isP2 -> FlameOrange
                                            else -> ArcadeBorder
                                        },
                                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp)
                                    )
                                    .clickable {
                                        selectedPlayer = fighter
                                    }
                                    .testTag("roster_item_${fighter.id}")
                            ) {
                                Image(
                                    painter = painterResource(id = fighter.portraitResId),
                                    contentDescription = fighter.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // P1 / P2 badges
                                if (isP1) {
                                    Surface(
                                        color = RiftGreen,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            "P1",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                if (isP2) {
                                    Surface(
                                        color = FlameOrange,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    ) {
                                        Text(
                                            "CPU",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Switch / Randomize Opponent
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val remaining = FighterRegistry.ALL_FIGHTERS.filter { it.id != selectedEnemy.id }
                                selectedEnemy = remaining.random()
                            },
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcadeTextLight),
                            modifier = Modifier.testTag("shuffle_opponent")
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CHANGE OPPONENT", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Launch Button
                    Button(
                        onClick = {
                            onStartBattle(selectedPlayer, selectedEnemy, selectedStage)
                        },
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RiftGreen,
                            contentColor = Color(0xFF101908)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .testTag("start_fight_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENTER THE ARENA",
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }

                // Right Pane: Opponent Preview Card
                FighterPreviewCard(
                    title = if (gameMode == GameMode.TRAINING) "TRAINING DUMMY" else "OPPONENT",
                    character = selectedEnemy,
                    isPlayerOne = false,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
private fun StageMiniPicker(
    selectedStage: StageDef,
    onSelectStage: (StageDef) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
            color = Color(0xFF1B2319),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
            modifier = Modifier.testTag("stage_picker")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STAGE: ${selectedStage.name.uppercase()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RiftGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = RiftGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF182015))
        ) {
            StageRegistry.ALL_STAGES.forEach { stage ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(stage.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(stage.subtitle, color = ArcadeTextMuted, fontSize = 9.sp)
                        }
                    },
                    onClick = {
                        onSelectStage(stage)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FighterPreviewCard(
    title: String,
    character: CharacterDef,
    isPlayerOne: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
        color = Color(0xFF161E14),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlayerOne) RiftGreen.copy(alpha = 0.6f) else FlameOrange.copy(alpha = 0.6f)
        ),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlayerOne) RiftGreen else FlameOrange,
                    letterSpacing = 1.sp
                )
                Text(
                    text = character.archetype.name.replace("_", " "),
                    fontSize = 9.sp,
                    color = ArcadeTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Portrait image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 10.dp))
                    .border(1.dp, character.primaryColor, CutCornerShape(topStart = 0.dp, bottomEnd = 10.dp))
            ) {
                Image(
                    painter = painterResource(id = character.portraitResId),
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = character.name.uppercase(),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = character.subtitle,
                fontSize = 9.sp,
                color = character.primaryColor,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Move badge preview
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF222C1D),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text(
                        text = "SPECIAL: ${character.specialMove.name}",
                        color = character.primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SUPER: ${character.superArt.name}",
                        color = ArcadeGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
