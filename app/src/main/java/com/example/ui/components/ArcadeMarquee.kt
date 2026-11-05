package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArcadeMarquee(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marquee_lights")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val bulbColors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF4081), // Pink
        Color(0xFF00E676), // Lime green
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF9100), // Orange
        Color(0xFFE040FB)  // Purple
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer curved cyan marquee frame with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF29B6F6), Color(0xFF0288D1), Color(0xFF01579B))
                    )
                )
                .border(3.dp, Color(0xFF81D4FA), RoundedCornerShape(28.dp))
                .padding(vertical = if (compact) 6.dp else 8.dp, horizontal = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row of fairy light bulbs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..7) {
                        val activeColorIndex = ((i + phase.toInt()) % bulbColors.size)
                        val lightColor = bulbColors[activeColorIndex]
                        Box(
                            modifier = Modifier
                                .size(if (compact) 10.dp else 14.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(lightColor)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Inner marquee sign board
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .padding(top = 4.dp, bottom = 4.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFE1F5FE),
                                    Color(0xFFB3E5FC),
                                    Color(0xFF81D4FA)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFFFFF9C4), RoundedCornerShape(18.dp))
                        .padding(vertical = if (compact) 2.dp else 4.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left cute mole icon badge
                        Text(
                            text = "🐹",
                            fontSize = if (compact) 20.sp else 28.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        // 3D Cartoon Title Text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // "¡GOLPEA" in warm vibrant red/orange with 3D drop shadow
                            Box {
                                Text(
                                    text = "¡GOLPEA",
                                    fontSize = if (compact) 19.sp else 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB71C1C),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                                )
                                Text(
                                    text = "¡GOLPEA",
                                    fontSize = if (compact) 19.sp else 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF3D00),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // "AL TOPO!" in lime-yellow gradient style
                            Box {
                                Text(
                                    text = "AL TOPO!",
                                    fontSize = if (compact) 21.sp else 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1B5E20),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 2.dp, y = 2.dp)
                                )
                                Text(
                                    text = "AL TOPO!",
                                    fontSize = if (compact) 21.sp else 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF76FF03),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Right cute builder mole icon badge
                        Text(
                            text = "👷",
                            fontSize = if (compact) 20.sp else 28.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                // Bottom row of fairy light bulbs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 7 downTo 0) {
                        val activeColorIndex = ((i + phase.toInt()) % bulbColors.size)
                        val lightColor = bulbColors[activeColorIndex]
                        Box(
                            modifier = Modifier
                                .size(if (compact) 10.dp else 14.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(lightColor)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
