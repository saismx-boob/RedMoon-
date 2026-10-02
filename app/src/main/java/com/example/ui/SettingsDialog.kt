package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.core.AudioSynthesizer
import com.example.game.core.HapticManager
import com.example.game.model.AiDifficulty
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    currentDifficulty: AiDifficulty,
    onDifficultyChanged: (AiDifficulty) -> Unit,
    audio: AudioSynthesizer,
    haptic: HapticManager?,
    onDismiss: () -> Unit
) {
    var isMuted by remember { mutableStateOf(audio.isMuted()) }
    var isHapticEnabled by remember { mutableStateOf(haptic?.isEnabled ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 16.dp),
            color = Color(0xF2141C12),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RiftGreen),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ARCADE SETTINGS",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = RiftGreen)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. CPU Difficulty
                Text(
                    text = "CPU AI DIFFICULTY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RiftGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiDifficulty.values().forEach { diff ->
                        val isSelected = currentDifficulty == diff
                        Surface(
                            onClick = { onDifficultyChanged(diff) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) RiftGreen else Color(0xFF1E2819),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color.White else ArcadeBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("diff_${diff.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = diff.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Sound Effects
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SYNTH ARCADE AUDIO", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Low-latency retro combat sound effects", color = ArcadeTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = !isMuted,
                        onCheckedChange = { checked ->
                            isMuted = !checked
                            audio.setMuted(!checked)
                            if (checked) audio.playSound("round_start")
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = RiftGreen)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HAPTIC IMPACT VIBRATION", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Physical vibration pulses on hits, blocks & supers", color = ArcadeTextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = isHapticEnabled,
                        onCheckedChange = { checked ->
                            isHapticEnabled = checked
                            haptic?.isEnabled = checked
                            if (checked) haptic?.onHeavyHit()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = RiftGreen)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. About & Version Info
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B2417),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("KAIRO: THE LAST STAND (2D FIGHTER)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Native Android 60 FPS fighting game engine with procedural arcade audio, full combo cancel mechanics, 4 characters, 4 stages, and dynamic particles.", color = ArcadeTextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("VERSION 1.0.0 • ANDROID READY", color = ArcadeGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
