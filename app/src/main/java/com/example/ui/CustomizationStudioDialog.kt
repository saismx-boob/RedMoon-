package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.game.content.StageRegistry
import com.example.game.model.CharacterDef
import com.example.game.model.CostumePalette
import com.example.game.model.StageDef
import com.example.ui.theme.*

enum class StudioTab {
    SPRITES_SKINS,
    STAGES_ARENAS,
    DEVELOPER_GUIDE
}

@Composable
fun CustomizationStudioDialog(
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(StudioTab.SPRITES_SKINS) }
    var selectedFighter by remember { mutableStateOf(FighterRegistry.KAIRO) }
    var selectedStage by remember { mutableStateOf(StageRegistry.ASHEN_RUINS) }
    var isAttackPose by remember { mutableStateOf(false) }
    var selectedPalette by remember { mutableStateOf(CostumePalette.CLASSIC_90S) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CutCornerShape(topStart = 0.dp, bottomEnd = 16.dp),
            color = Color(0xF5131A11),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RiftGreen),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .testTag("studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ATELIER DES DESSINS & DÉCORS",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "CUSTOMISEZ LES SPRITES, DÉCORS & DÉCOUVREZ COMMENT LES REMPLACER",
                            fontSize = 9.sp,
                            color = ArcadeTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_studio")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = RiftGreen)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioTabButton(
                        title = "DESSINS & COSTUMES",
                        isSelected = activeTab == StudioTab.SPRITES_SKINS,
                        onClick = { activeTab = StudioTab.SPRITES_SKINS },
                        modifier = Modifier.weight(1f)
                    )
                    StudioTabButton(
                        title = "DÉCORS & ARÈNES",
                        isSelected = activeTab == StudioTab.STAGES_ARENAS,
                        onClick = { activeTab = StudioTab.STAGES_ARENAS },
                        modifier = Modifier.weight(1f)
                    )
                    StudioTabButton(
                        title = "GUIDE TECHNIQUE",
                        isSelected = activeTab == StudioTab.DEVELOPER_GUIDE,
                        onClick = { activeTab = StudioTab.DEVELOPER_GUIDE },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content View based on Tab
                when (activeTab) {
                    StudioTab.SPRITES_SKINS -> {
                        SpritesAndSkinsTab(
                            selectedFighter = selectedFighter,
                            onSelectFighter = { selectedFighter = it },
                            isAttackPose = isAttackPose,
                            onTogglePose = { isAttackPose = it },
                            selectedPalette = selectedPalette,
                            onSelectPalette = { selectedPalette = it }
                        )
                    }

                    StudioTab.STAGES_ARENAS -> {
                        StagesShowcaseTab(
                            selectedStage = selectedStage,
                            onSelectStage = { selectedStage = it }
                        )
                    }

                    StudioTab.DEVELOPER_GUIDE -> {
                        DeveloperGuideTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp),
        color = if (isSelected) RiftGreen else Color(0xFF1E2818),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color.White else ArcadeBorder),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun SpritesAndSkinsTab(
    selectedFighter: CharacterDef,
    onSelectFighter: (CharacterDef) -> Unit,
    isAttackPose: Boolean,
    onTogglePose: (Boolean) -> Unit,
    selectedPalette: CostumePalette,
    onSelectPalette: (CostumePalette) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Full-Body Sprite Showcase
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp))
                .background(Color(0xFF101410))
                .border(1.5.dp, selectedFighter.primaryColor, CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp))
        ) {
            val spriteResId = if (isAttackPose) selectedFighter.attackSpriteResId else selectedFighter.idleSpriteResId

            Image(
                painter = painterResource(id = spriteResId),
                contentDescription = selectedFighter.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            // Fighter Label Overlay
            Surface(
                color = Color(0xCC151D12),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedFighter.primaryColor),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = selectedFighter.name.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isAttackPose) "POSE : FRAPPE SPÉCIALE" else "POSE : POSITION DE COMBAT (IDLE)",
                        color = selectedFighter.primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right Column: Controls & Selectors
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Fighter Selector
            Text("CHOISIR LE PERSONNAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RiftGreen)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FighterRegistry.ALL_FIGHTERS.forEach { fighter ->
                    val isCur = selectedFighter.id == fighter.id
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp))
                            .border(
                                width = if (isCur) 2.dp else 1.dp,
                                color = if (isCur) fighter.primaryColor else ArcadeBorder,
                                shape = CutCornerShape(topStart = 0.dp, bottomEnd = 6.dp)
                            )
                            .clickable { onSelectFighter(fighter) }
                    ) {
                        Image(
                            painter = painterResource(id = fighter.portraitResId),
                            contentDescription = fighter.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. Pose Switcher
            Text("POSE D'ANIMATION 2D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RiftGreen)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onTogglePose(false) },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isAttackPose) selectedFighter.primaryColor else Color(0xFF1E2818),
                        contentColor = if (!isAttackPose) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("GARDE (IDLE)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onTogglePose(true) },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAttackPose) selectedFighter.primaryColor else Color(0xFF1E2818),
                        contentColor = if (isAttackPose) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("FRAPPE / COMBO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3. Costume / Palette Swap
            Text("PALETTE DE COULEURS / COSTUME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RiftGreen)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CostumePalette.values().forEach { palette ->
                    val isSel = selectedPalette == palette
                    Surface(
                        onClick = { onSelectPalette(palette) },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSel) Color(0xFF283622) else Color(0xFF192216),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) RiftGreen else Color(0xFF2A3626)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(palette.tintColor ?: selectedFighter.primaryColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = palette.displayName,
                                color = if (isSel) Color.White else ArcadeTextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StagesShowcaseTab(
    selectedStage: StageDef,
    onSelectStage: (StageDef) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stage Big Preview
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp))
                .border(1.5.dp, selectedStage.primaryAtmosphereColor, CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp))
        ) {
            Image(
                painter = painterResource(id = selectedStage.backgroundResId),
                contentDescription = selectedStage.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Stage Info Badge
            Surface(
                color = Color(0xDD121A10),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedStage.primaryAtmosphereColor),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = selectedStage.name.uppercase(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = selectedStage.subtitle,
                        color = selectedStage.primaryAtmosphereColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "EFFET AMBIANT : ${selectedStage.ambientParticle.name.replace("_", " ")}",
                        color = ArcadeTextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Stage List
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("CHOIX DU DÉCOR 2D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RiftGreen)

            StageRegistry.ALL_STAGES.forEach { stage ->
                val isSel = selectedStage.id == stage.id
                Surface(
                    onClick = { onSelectStage(stage) },
                    shape = CutCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                    color = if (isSel) Color(0xFF283622) else Color(0xFF182215),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) stage.primaryAtmosphereColor else Color(0xFF283624)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = stage.backgroundResId),
                            contentDescription = stage.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp, 36.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stage.name,
                                color = if (isSel) Color.White else ArcadeTextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = stage.subtitle,
                                color = ArcadeTextMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeveloperGuideTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E2819),
            border = androidx.compose.foundation.BorderStroke(1.dp, RiftGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = RiftGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GUIDE TECHNIQUE : CHANGER VOS DESSINS & DÉCORS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                GuideStep(
                    stepNumber = "1",
                    title = "Emplacement des fichiers d'images",
                    description = "Tous les sprites et arrière-plans sont stockés dans le dossier standard Android :\napp/src/main/res/drawable/"
                )

                GuideStep(
                    stepNumber = "2",
                    title = "Formats recommandés",
                    description = "• Personnages : PNG 32-bit avec transparence alpha (format 3:4, ex: 600x800 px ou 800x1066 px).\n• Décors : JPG ou PNG 16:9 (ex: 1920x1080 px ou 1440x810 px)."
                )

                GuideStep(
                    stepNumber = "3",
                    title = "Associer les images dans le code",
                    description = "Ouvrez FighterRegistry.kt pour lier vos fichiers :\n• idleSpriteResId = R.drawable.votre_perso_idle\n• attackSpriteResId = R.drawable.votre_perso_attack\n• portraitResId = R.drawable.votre_portrait\n\nPour les décors, ouvrez StageRegistry.kt et modifiez :\n• backgroundResId = R.drawable.votre_nouveau_decor"
                )

                GuideStep(
                    stepNumber = "4",
                    title = "Ajuster la portée et les hitboxes",
                    description = "Dans MoveData.kt et FighterRegistry.kt, vous pouvez ajuster la portée (reach), la hauteur du coup et les dégâts pour que les hitboxes s'alignent au pixel près sur vos nouvelles armes ou coups de pied."
                )
            }
        }
    }
}

@Composable
private fun GuideStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = RiftGreen,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stepNumber, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color(0xFFF1F5E8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            color = ArcadeTextMuted,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 28.dp)
        )
    }
}
