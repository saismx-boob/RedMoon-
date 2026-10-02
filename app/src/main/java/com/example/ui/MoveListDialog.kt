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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.SportsKabaddi
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
import androidx.compose.ui.window.Dialog
import com.example.game.content.FighterRegistry
import com.example.game.core.MoveData
import com.example.game.model.CharacterDef
import com.example.ui.theme.*

@Composable
fun MoveListDialog(
    onDismiss: () -> Unit
) {
    var selectedFighter by remember { mutableStateOf(FighterRegistry.KAIRO) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 16.dp),
            color = Color(0xF2141C12),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RiftGreen),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("movelist_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FIGHTER ARCHIVE & MOVELIST",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "FRAME DATA, SIGNATURE SPECIALS, AND CANCEL CHAINS",
                            fontSize = 9.sp,
                            color = ArcadeTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_movelist")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = RiftGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fighter Selection Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FighterRegistry.ALL_FIGHTERS.forEach { fighter ->
                        val isSelected = selectedFighter.id == fighter.id
                        Surface(
                            onClick = { selectedFighter = fighter },
                            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
                            color = if (isSelected) fighter.primaryColor else Color(0xFF1E2819),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color.White else ArcadeBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_${fighter.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = fighter.portraitResId),
                                    contentDescription = fighter.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = fighter.name.uppercase(),
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Move Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Bio & Fighting Style Banner
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B2417),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${selectedFighter.name.uppercase()} • ${selectedFighter.subtitle}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = selectedFighter.primaryColor
                                )
                                Text(
                                    text = "STYLE: ${selectedFighter.fightingStyle.uppercase()}",
                                    fontSize = 10.sp,
                                    color = ArcadeGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedFighter.bio,
                                color = ArcadeTextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Normal Attacks Frame Data
                    Text(
                        text = "NORMAL ATTACKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    MoveRow(move = selectedFighter.lightPunch, button = "LP", color = Color(0xFF93C5FD))
                    MoveRow(move = selectedFighter.heavyPunch, button = "HP", color = Color(0xFF3B82F6))
                    MoveRow(move = selectedFighter.lightKick, button = "LK", color = Color(0xFFFCA5A5))
                    MoveRow(move = selectedFighter.heavyKick, button = "HK", color = FlameCrimson)

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Special Moves & Super Arts
                    Text(
                        text = "SPECIAL MOVES & SUPER ARTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    SpecialMoveCard(
                        name = selectedFighter.specialMove.name,
                        command = selectedFighter.specialMove.commandHint,
                        description = selectedFighter.specialMove.description,
                        damage = "${selectedFighter.specialMove.moveData.damage.toInt()} DMG",
                        accentColor = selectedFighter.primaryColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SpecialMoveCard(
                        name = selectedFighter.superArt.name,
                        command = selectedFighter.superArt.commandHint,
                        description = selectedFighter.superArt.description,
                        damage = "${selectedFighter.superArt.moveData.damage.toInt()} DMG",
                        accentColor = ArcadeGold,
                        isSuper = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Pro Combo Tips Box
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E2818),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RiftGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = RiftGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PRO COMBO CHAIN TIPS",
                                    color = RiftGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Chain: LP -> HP -> Special Move -> Super Art! Cancel windows activate on hit. Counter-hits grant +25% bonus damage and frame stun.",
                                    color = ArcadeTextLight,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveRow(move: MoveData, button: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF161E14),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF283424)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = button,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(move.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (move.isLauncher) {
                        Text("LAUNCHER (JUGGLE)", color = FlameOrange, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    } else if (move.isLow) {
                        Text("LOW ATTACK", color = Color(0xFF93C5FD), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${move.startupFrames}f Startup", color = ArcadeTextMuted, fontSize = 9.sp)
                Text("${move.damage.toInt()} DMG", color = Color(0xFFFDE047), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SpecialMoveCard(
    name: String,
    command: String,
    description: String,
    damage: String,
    accentColor: Color,
    isSuper: Boolean = false
) {
    Surface(
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
        color = if (isSuper) Color(0xFF2B200A) else Color(0xFF182315),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            command,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(damage, color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = ArcadeTextMuted, fontSize = 10.sp)
        }
    }
}
