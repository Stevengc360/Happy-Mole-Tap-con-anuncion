package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val initialYRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFFEB3B), // Yellow
            Color(0xFFFF4081), // Pink
            Color(0xFF00E676), // Green
            Color(0xFF00B0FF), // Light blue
            Color(0xFFFF9100), // Orange
            Color(0xFFE040FB)  // Purple
        )
        List(45) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                initialYRatio = -0.2f - Random.nextFloat() * 0.3f,
                speed = 0.8f + Random.nextFloat() * 0.5f,
                size = 14f + Random.nextFloat() * 14f,
                color = colors.random(),
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (p in particles) {
            val currentY = (p.initialYRatio + progress.value * p.speed * 1.5f) * height
            val currentX = (p.xRatio + kotlin.math.sin(progress.value * 10f + p.xRatio * 5f) * 0.05f) * width

            if (currentY in 0f..height) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2f,
                        center = Offset(currentX, currentY)
                    )
                } else {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(currentX, currentY),
                        size = Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}
