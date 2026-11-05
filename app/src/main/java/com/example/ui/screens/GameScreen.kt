package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.model.Mole
import com.example.ui.components.ArcadeMallet
import com.example.ui.components.ArcadeMoleHole
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.PersistentScoreboard
import com.example.ui.components.TicketDispenser
import com.example.viewmodel.FloatingHitEffect
import com.example.viewmodel.GameUiState

@Composable
fun GameScreen(
    state: GameUiState,
    onWhackMole: (Int) -> Unit,
    onTapEmptySpace: () -> Unit = {},
    onPauseToggle: () -> Unit,
    onReturnToMenu: () -> Unit,
    onToggleSfx: () -> Unit,
    onRestartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onReturnToMenu)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFB300), Color(0xFFFFA000), Color(0xFFFF8F00))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PersistentScoreboard(
                score = state.score,
                hits = state.hits,
                misses = state.misses,
                timeLeftSeconds = state.timeLeftSeconds,
                combo = state.combo,
                ticketsWon = state.ticketsThisGame,
                difficulty = state.difficulty,
                isPaused = state.isPaused,
                sfxEnabled = state.sfxEnabled,
                onReturnToMenu = onReturnToMenu,
                onPauseToggle = onPauseToggle,
                onToggleSfx = onToggleSfx,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            MoleBoard(
                state = state,
                onWhackMole = onWhackMole,
                onTapEmptySpace = onTapEmptySpace,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.96f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            TicketDispenser(
                ticketsWon = state.ticketsThisGame,
                totalTickets = state.totalTickets
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        AchievementBanner(
            achievement = state.newlyUnlockedAchievement,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        if (state.isPaused) {
            PauseOverlay(onResume = onPauseToggle, onReturnToMenu = onReturnToMenu)
        }

        if (state.isGameOver) {
            GameOverOverlay(
                score = state.score,
                hits = state.hits,
                ticketsWon = state.ticketsThisGame,
                highScore = state.highScore,
                onRestart = onRestartGame,
                onMenu = onReturnToMenu
            )
        }
    }
}

@Composable
private fun MoleBoard(
    state: GameUiState,
    onWhackMole: (Int) -> Unit,
    onTapEmptySpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFF43A047), Color(0xFF2E7D32))
                )
            )
            .border(4.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapEmptySpace
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (rowStart in listOf(0, 3)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (holeIndex in rowStart until rowStart + 3) {
                        MoleCell(
                            holeIndex = holeIndex,
                            state = state,
                            onWhackMole = onWhackMole,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        state.floatingEffects.firstOrNull { it.holeIndex == null }?.let { effect ->
            FloatingEffectBadge(
                effect = effect,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun MoleCell(
    holeIndex: Int,
    state: GameUiState,
    onWhackMole: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mole = state.moles.getOrElse(holeIndex) { Mole(id = holeIndex) }
    val effect = state.floatingEffects.firstOrNull { it.holeIndex == holeIndex }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        ArcadeMoleHole(
            mole = mole,
            onWhack = { onWhackMole(holeIndex) },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.activeMalletHole == holeIndex) {
            ArcadeMallet(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-14).dp)
            )
        }

        effect?.let {
            FloatingEffectBadge(
                effect = it,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-28).dp)
            )
        }
    }
}

@Composable
private fun FloatingEffectBadge(
    effect: FloatingHitEffect,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        effect.isMiss -> Color(0xDD37474F)
        effect.isCritical -> Color(0xFFFFD700)
        else -> Color(0xFFFF1744)
    }
    val textColor = when {
        effect.isMiss -> Color(0xFFECEFF1)
        effect.isCritical -> Color(0xFFB71C1C)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = effect.text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

@Composable
private fun AchievementBanner(
    achievement: Achievement?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = achievement != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        achievement?.let { ach ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF9800), Color(0xFFFF5722))
                        )
                    )
                    .border(3.dp, Color(0xFFFFF9C4), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = ach.icon, fontSize = 34.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🏆 ¡LOGRO DESBLOQUEADO!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5D4037)
                        )
                        Text(
                            text = ach.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "+${ach.rewardTickets} tickets de premio 🎉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF9C4)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏸️ JUEGO EN PAUSA",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("▶️ Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onReturnToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🏠 Menú Principal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    hits: Int,
    ticketsWon: Int,
    highScore: Int,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB000000)),
        contentAlignment = Alignment.Center
    ) {
        ConfettiOverlay()

        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFE082), Color(0xFFFFCA28))
                    )
                )
                .border(4.dp, Color(0xFFFF9800), RoundedCornerShape(28.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉 ¡TIEMPO!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFD84315)
            )
            Text(
                text = "¡Excelente partida!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when {
                    score >= 250 -> "⭐⭐⭐"
                    score >= 120 -> "⭐⭐"
                    else -> "⭐"
                },
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameResultCard(score = score, hits = hits, ticketsWon = ticketsWon, highScore = highScore)

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("play_again_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("🔄 ¡Jugar otra vez!", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("game_over_menu_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("🏠 Volver al Menú", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun GameResultCard(
    score: Int,
    hits: Int,
    ticketsWon: Int,
    highScore: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ResultRow("⭐ Puntuación:", "$score pts", Color(0xFFE65100))
        Spacer(modifier = Modifier.height(6.dp))
        ResultRow("🎯 Topos golpeados:", "$hits", Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(6.dp))
        ResultRow("🎟️ Tickets ganados:", "+$ticketsWon", Color(0xFFD81B60))

        if (score >= highScore && score > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🔥 ¡NUEVO RÉCORD PERSONAL! 🔥",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFC2185B)
            )
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
        Text(text = value, fontWeight = FontWeight.Black, color = valueColor, fontSize = 16.sp)
    }
}
