package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty

/**
 * Persistent Real-Time Scoreboard UI component anchored at the top of the game screen.
 * Updates in real-time as moles are hit:
 * - Live animated Score with pulse effect on each hit
 * - Live Hits (Aciertos) counter in top-left
 * - Live Score progress bar in top-right
 * - Live Combo multiplier badge (🔥 COMBO x3!)
 * - Live digital LED arcade displays for PUNTOS and TIEMPO
 * - Real-time tickets won indicator
 * - Integrated navigation & audio controls
 */
@Composable
fun PersistentScoreboard(
    score: Int,
    hits: Int,
    misses: Int,
    timeLeftSeconds: Int,
    combo: Int,
    ticketsWon: Int,
    difficulty: Difficulty,
    isPaused: Boolean,
    sfxEnabled: Boolean,
    onReturnToMenu: () -> Unit,
    onPauseToggle: () -> Unit,
    onToggleSfx: () -> Unit,
    targetScore: Int = 300,
    modifier: Modifier = Modifier
) {
    // Pulse animation when score updates in real-time
    var prevScore by remember { mutableIntStateOf(score) }
    var scorePulse by remember { mutableStateOf(false) }

    LaunchedEffect(score) {
        if (score > prevScore) {
            scorePulse = true
            prevScore = score
            kotlinx.coroutines.delay(180L)
            scorePulse = false
        } else {
            prevScore = score
        }
    }

    val animatedScoreScale by animateFloatAsState(
        targetValue = if (scorePulse) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "score_pulse"
    )

    // Pulse animation when hits count updates
    var prevHits by remember { mutableIntStateOf(hits) }
    var hitsPulse by remember { mutableStateOf(false) }

    LaunchedEffect(hits) {
        if (hits > prevHits) {
            hitsPulse = true
            prevHits = hits
            kotlinx.coroutines.delay(180L)
            hitsPulse = false
        } else {
            prevHits = hits
        }
    }

    val animatedHitsScale by animateFloatAsState(
        targetValue = if (hitsPulse) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "hits_pulse"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progress_bar"
    )

    val isUrgentTime = timeLeftSeconds <= 5
    val infiniteTransition = rememberInfiniteTransition(label = "urgent_time")
    val urgentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgent_alpha"
    )

    // Persistent Arcade Scoreboard Panel
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_scoreboard")
            .shadow(10.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFD54F), // Bright sunny gold
                        Color(0xFFFFCA28),
                        Color(0xFFFFB300)
                    )
                )
            )
            .border(
                3.dp,
                Color(0xFFFF8F00),
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // ROW 1: Controls (Back, Difficulty chip, Tickets badge, Sound, Pause)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back to menu button
            IconButton(
                onClick = onReturnToMenu,
                modifier = Modifier
                    .size(36.dp)
                    .shadow(3.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Menú",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Center status: Difficulty & Real-time Tickets
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Difficulty Chip
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(difficulty.colorHex))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${difficulty.icon} ${difficulty.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                // Live Tickets Won Badge
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF7043), Color(0xFFE64A19))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎟️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+$ticketsWon",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // Sound and Pause buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onToggleSfx,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(3.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sonido",
                        tint = if (sfxEnabled) Color(0xFF2E7D32) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(3.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Continuar" else "Pausar",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ROW 2: Required counters:
        // Top-Left: "el numero de golpes aciertos en la esquina superior izquierda"
        // Top-Right: "Una barra de puntucion arriba a la derecha"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Esquina superior izquierda: GOLPES / ACIERTOS (con pulso en tiempo real)
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF5722), Color(0xFFFF8A65))
                        )
                    )
                    .border(2.dp, Color(0xFFFFCCBC), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎯", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Column {
                        Text(
                            text = "ACIERTOS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF9C4)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$hits",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.scale(animatedHitsScale)
                            )
                            if (misses > 0) {
                                Text(
                                    text = " / $misses❌",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFCCBC),
                                    modifier = Modifier.padding(start = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Live Combo Multiplier badge (appears & pulses in real time)
            AnimatedVisibility(
                visible = combo >= 2,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFF1744), Color(0xFFFF9100))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFFEB3B), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "x$combo! 🔥",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFEB3B)
                    )
                }
            }

            // Esquina superior derecha: Barra de Puntuación en tiempo real
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF303F9F), Color(0xFF3F51B5))
                        )
                    )
                    .border(2.dp, Color(0xFFC5CAE9), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ PUNTUACIÓN",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "$score pts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.scale(animatedScoreScale)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    // Real-time progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1A237E))
                            .border(1.dp, Color(0xFF7986CB), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFEB3B),
                                            Color(0xFFFF9800),
                                            Color(0xFFFF3D00)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ROW 3: Real-Time Digital LED Displays (PUNTOS & TIEMPO)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFE082), Color(0xFFFFCA28))
                    )
                )
                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real-Time PUNTOS LED box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PUNTOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
                Box(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF121212))
                        .border(1.5.dp, Color(0xFF333333), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score.toString().padStart(4, '0'),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF1744), // Glowing red 7-segment LED
                        modifier = Modifier.scale(animatedScoreScale)
                    )
                }
            }

            // Divider dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF8F00))
            )

            // Real-Time TIEMPO LED box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TIEMPO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
                val minutes = timeLeftSeconds / 60
                val seconds = timeLeftSeconds % 60
                val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

                Box(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF121212))
                        .border(
                            1.5.dp,
                            if (isUrgentTime) Color(0xFFFF1744) else Color(0xFF333333),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgentTime) {
                            Color(0xFFFF1744).copy(alpha = urgentAlpha)
                        } else {
                            Color(0xFFFF9100) // Amber LED
                        }
                    )
                }
            }
        }
    }
}
