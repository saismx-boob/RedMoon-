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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.GameMode
import com.example.ui.theme.*

@Composable
fun ArcadeTitleScreen(
    onSelectMode: (GameMode) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStudio: () -> Unit,
    survivalBestStreak: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDarkBg)
    ) {
        // Hero Background Splash
        Image(
            painter = painterResource(id = R.drawable.title_hero_banner_1790969351879),
            contentDescription = "Kairo Fighting Game Battle Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Vignette & Gradient Overlays for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0D120B),
                            Color(0x66141614),
                            Color(0xF0101410)
                        )
                    )
                )
        )

        // Main Title Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Title & Lore
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Header badge
                Surface(
                    shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                    color = Color(0xFF1E2818),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiftGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "90s RETRO 2D FIGHTING ENGINE",
                        color = RiftGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Title
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "KAIRO",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = " / ",
                        fontSize = 42.sp,
                        color = FlameOrange,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "THE LAST STAND",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = RiftGreen,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "Master the art of 2D combat. Branching combo chains, frame cancels, aerial juggles, and destructive Super Arts.",
                    color = ArcadeTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.widthIn(max = 480.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Feature Highlights
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeaturePill(icon = Icons.Default.Bolt, title = "4 Fighters")
                    FeaturePill(icon = Icons.Default.Landscape, title = "4 Stages")
                    FeaturePill(icon = Icons.Default.Speed, title = "60 FPS Native")
                    if (survivalBestStreak > 0) {
                        FeaturePill(icon = Icons.Default.EmojiEvents, title = "Best: $survivalBestStreak Wins")
                    }
                }
            }

            // Right Side: Game Mode Selection Buttons
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                MenuButton(
                    title = "ARCADE MODE",
                    subtitle = "Story ladder to the final titan",
                    accentColor = RiftGreen,
                    testTag = "menu_arcade",
                    onClick = { onSelectMode(GameMode.ARCADE) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MenuButton(
                    title = "VERSUS MODE",
                    subtitle = "Free battle vs CPU or CPU exhibition",
                    accentColor = FlameOrange,
                    testTag = "menu_versus",
                    onClick = { onSelectMode(GameMode.VERSUS) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MenuButton(
                    title = "SURVIVAL GAUNTLET",
                    subtitle = "Endless ladder with escalating challenge",
                    accentColor = FlameCrimson,
                    testTag = "menu_survival",
                    onClick = { onSelectMode(GameMode.SURVIVAL) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                MenuButton(
                    title = "COMBAT DOJO",
                    subtitle = "Practice combos & view frame hitboxes",
                    accentColor = CyberPurple,
                    testTag = "menu_dojo",
                    onClick = { onSelectMode(GameMode.TRAINING) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Tools Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenStudio,
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RiftGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RiftGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("menu_studio")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Atelier & Décors", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ATELIER DESSINS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenArchive,
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                        modifier = Modifier.testTag("menu_archive")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Fighter Archive", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MOVELIST", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeBorder),
                        modifier = Modifier.testTag("menu_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OPTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B2318),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32402A))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = RiftGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, color = Color(0xFFE2E8F0), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
        color = Color(0xFF182015),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .width(320.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    color = ArcadeTextMuted,
                    fontSize = 10.sp
                )
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
