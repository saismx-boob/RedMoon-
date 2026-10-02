package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.content.FighterRegistry
import com.example.game.content.StageRegistry
import com.example.game.core.AudioSynthesizer
import com.example.game.core.FightEngine
import com.example.game.core.HapticManager
import com.example.game.model.AiDifficulty
import com.example.game.model.CharacterDef
import com.example.game.model.GameMode
import com.example.game.model.StageDef
import com.example.ui.ArcadeTitleScreen
import com.example.ui.CharacterSelectScreen
import com.example.ui.CustomizationStudioDialog
import com.example.ui.FightScreen
import com.example.ui.MoveListDialog
import com.example.ui.SettingsDialog
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    TITLE,
    CHARACTER_SELECT,
    FIGHT
}

class MainActivity : ComponentActivity() {

    private val audioSynthesizer by lazy { AudioSynthesizer() }
    private val hapticManager by lazy { HapticManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("kairo_fight_prefs", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.TITLE) }
                var selectedMode by remember { mutableStateOf(GameMode.ARCADE) }
                var selectedDifficulty by remember {
                    val savedDiff = prefs.getString("ai_difficulty", AiDifficulty.NORMAL.name)
                    mutableStateOf(
                        try { AiDifficulty.valueOf(savedDiff ?: "NORMAL") }
                        catch (_: Exception) { AiDifficulty.NORMAL }
                    )
                }
                var survivalBestStreak by remember {
                    mutableStateOf(prefs.getInt("survival_best_streak", 0))
                }

                var showMoveListDialog by remember { mutableStateOf(false) }
                var showSettingsDialog by remember { mutableStateOf(false) }
                var showStudioDialog by remember { mutableStateOf(false) }

                var fightEngine by remember { mutableStateOf<FightEngine?>(null) }

                // Handle Hardware Back Button
                BackHandler(enabled = currentScreen != ScreenState.TITLE || showMoveListDialog || showSettingsDialog || showStudioDialog) {
                    when {
                        showStudioDialog -> showStudioDialog = false
                        showMoveListDialog -> showMoveListDialog = false
                        showSettingsDialog -> showSettingsDialog = false
                        currentScreen == ScreenState.CHARACTER_SELECT -> currentScreen = ScreenState.TITLE
                        currentScreen == ScreenState.FIGHT -> {
                            fightEngine?.let { engine ->
                                if (engine.snapshot.value.survivalWins > survivalBestStreak) {
                                    survivalBestStreak = engine.snapshot.value.survivalWins
                                    prefs.edit().putInt("survival_best_streak", survivalBestStreak).apply()
                                }
                            }
                            fightEngine = null
                            currentScreen = ScreenState.TITLE
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        ScreenState.TITLE -> {
                            ArcadeTitleScreen(
                                onSelectMode = { mode ->
                                    selectedMode = mode
                                    currentScreen = ScreenState.CHARACTER_SELECT
                                },
                                onOpenArchive = { showMoveListDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                onOpenStudio = { showStudioDialog = true },
                                survivalBestStreak = survivalBestStreak
                            )
                        }

                        ScreenState.CHARACTER_SELECT -> {
                            CharacterSelectScreen(
                                gameMode = selectedMode,
                                onBack = { currentScreen = ScreenState.TITLE },
                                onStartBattle = { playerChar, enemyChar, stage ->
                                    fightEngine = FightEngine(
                                        playerCharacter = playerChar,
                                        enemyCharacter = enemyChar,
                                        stage = stage,
                                        gameMode = selectedMode,
                                        aiDifficulty = selectedDifficulty,
                                        audio = audioSynthesizer,
                                        haptic = hapticManager
                                    )
                                    currentScreen = ScreenState.FIGHT
                                }
                            )
                        }

                        ScreenState.FIGHT -> {
                            fightEngine?.let { engine ->
                                FightScreen(
                                    engine = engine,
                                    onExitToMenu = {
                                        if (engine.snapshot.value.survivalWins > survivalBestStreak) {
                                            survivalBestStreak = engine.snapshot.value.survivalWins
                                            prefs.edit().putInt("survival_best_streak", survivalBestStreak).apply()
                                        }
                                        fightEngine = null
                                        currentScreen = ScreenState.TITLE
                                    }
                                )
                            }
                        }
                    }

                    // Modals
                    if (showStudioDialog) {
                        CustomizationStudioDialog(onDismiss = { showStudioDialog = false })
                    }

                    if (showMoveListDialog) {
                        MoveListDialog(onDismiss = { showMoveListDialog = false })
                    }

                    if (showSettingsDialog) {
                        SettingsDialog(
                            currentDifficulty = selectedDifficulty,
                            onDifficultyChanged = { newDiff ->
                                selectedDifficulty = newDiff
                                prefs.edit().putString("ai_difficulty", newDiff.name).apply()
                            },
                            audio = audioSynthesizer,
                            haptic = hapticManager,
                            onDismiss = { showSettingsDialog = false }
                        )
                    }
                }
            }
        }
    }
}
