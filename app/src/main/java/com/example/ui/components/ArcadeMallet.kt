package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun ArcadeMallet(
    modifier: Modifier = Modifier
) {
    // Mallet strike animation: swings back then smashes down
    val rotation = remember { Animatable(-35f) }
    val scale = remember { Animatable(1.2f) }

    LaunchedEffect(Unit) {
        // Rapid slam down
        rotation.animateTo(
            targetValue = 18f,
            animationSpec = tween(90, easing = FastOutLinearInEasing)
        )
        scale.animateTo(
            targetValue = 0.95f,
            animationSpec = tween(60)
        )
        // Bounce back slightly
        rotation.animateTo(
            targetValue = -10f,
            animationSpec = tween(110)
        )
    }

    Box(
        modifier = modifier
            .size(90.dp)
            .rotate(rotation.value)
            .scale(scale.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Wooden handle
            val handleWidth = width * 0.12f
            val handleHeight = height * 0.55f
            drawRoundRect(
                color = Color(0xFFD7CCC8),
                topLeft = Offset(width * 0.65f, height * 0.40f),
                size = Size(handleWidth, handleHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Grip rings
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(width * 0.65f, height * 0.70f),
                size = Size(handleWidth, height * 0.18f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 2. Toy squeaky barrel hammer head (Red and Yellow horizontal stripes)
            val headLeft = width * 0.08f
            val headTop = height * 0.15f
            val headWidth = width * 0.68f
            val headHeight = height * 0.44f

            // Base barrel
            drawRoundRect(
                color = Color(0xFFD32F2F), // Red
                topLeft = Offset(headLeft, headTop),
                size = Size(headWidth, headHeight),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Yellow stripes across the barrel
            val stripeHeight = headHeight * 0.22f
            drawRect(
                color = Color(0xFFFFEB3B), // Yellow stripe 1
                topLeft = Offset(headLeft, headTop + headHeight * 0.22f),
                size = Size(headWidth, stripeHeight)
            )
            drawRect(
                color = Color(0xFFFFEB3B), // Yellow stripe 2
                topLeft = Offset(headLeft, headTop + headHeight * 0.62f),
                size = Size(headWidth, stripeHeight)
            )

            // Barrel end cushions
            drawOval(
                color = Color(0xFFFF5252),
                topLeft = Offset(headLeft - 6f, headTop + 4f),
                size = Size(12f, headHeight - 8f)
            )
            drawOval(
                color = Color(0xFFFF5252),
                topLeft = Offset(headLeft + headWidth - 6f, headTop + 4f),
                size = Size(12f, headHeight - 8f)
            )
        }
    }
}
