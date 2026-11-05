package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Mole
import com.example.model.MoleState
import com.example.model.MoleType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcadeMoleHole(
    mole: Mole,
    onWhack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moleOffsetY = remember { Animatable(100f) }
    val moleScaleX = remember { Animatable(1f) }
    val moleScaleY = remember { Animatable(1f) }
    val popScale = remember { Animatable(1f) }
    val shockwaveRadius = remember { Animatable(0.4f) }
    val shockwaveAlpha = remember { Animatable(0f) }

    LaunchedEffect(mole.state) {
        when (mole.state) {
            MoleState.UP -> {
                moleScaleX.snapTo(1f)
                moleScaleY.snapTo(1f)
                popScale.snapTo(1f)
                shockwaveAlpha.snapTo(0f)
                moleOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                )
            }
            MoleState.HIT -> {
                // Subtle, responsive & satisfying 'pop' animation on successful tap:
                // 1. Squash on impact
                launch {
                    moleScaleX.animateTo(1.18f, tween(50, easing = FastOutLinearInEasing))
                    moleScaleX.animateTo(0.92f, tween(70, easing = FastOutSlowInEasing))
                    moleScaleX.animateTo(1.0f, spring(dampingRatio = 0.55f, stiffness = 500f))
                }
                launch {
                    moleScaleY.animateTo(0.82f, tween(50, easing = FastOutLinearInEasing))
                    moleScaleY.animateTo(1.16f, tween(70, easing = FastOutSlowInEasing))
                    moleScaleY.animateTo(1.0f, spring(dampingRatio = 0.55f, stiffness = 500f))
                }
                // 2. Pop scale surge
                launch {
                    popScale.animateTo(1.14f, tween(50, easing = FastOutLinearInEasing))
                    popScale.animateTo(1.0f, spring(dampingRatio = 0.5f, stiffness = 450f))
                }
                // 3. Tactile dip and recovery
                launch {
                    moleOffsetY.animateTo(
                        targetValue = 8f,
                        animationSpec = tween(50, easing = FastOutSlowInEasing)
                    )
                    moleOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                    )
                }
                // 4. Subtle comic pop shockwave ring
                launch {
                    shockwaveAlpha.snapTo(0.85f)
                    shockwaveRadius.snapTo(0.35f)
                    shockwaveRadius.animateTo(1.25f, tween(260, easing = FastOutSlowInEasing))
                }
                launch {
                    shockwaveAlpha.animateTo(0f, tween(260, easing = LinearEasing))
                }
            }
            MoleState.HIDDEN, MoleState.RETREATING -> {
                moleScaleX.snapTo(1f)
                moleScaleY.snapTo(1f)
                popScale.snapTo(1f)
                shockwaveAlpha.snapTo(0f)
                moleOffsetY.animateTo(
                    targetValue = 100f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
            }
            else -> {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hit_stars")
    val starAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.15f)
            .padding(4.dp)
            .testTag("mole_hole_${mole.id}")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onWhack()
            },
        contentAlignment = Alignment.Center
    ) {
        // Base Hole graphic: Golden outer rim, deep brown inner hole
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHoleBezel(size)

            // Draw subtle comic pop shockwave ring on successful tap
            if (shockwaveAlpha.value > 0f) {
                val centerX = size.width / 2f
                val centerY = size.height * 0.60f
                val radius = size.width * 0.44f * shockwaveRadius.value

                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = shockwaveAlpha.value),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4.dp.toPx() * (1f - shockwaveRadius.value * 0.4f))
                )

                // Little radial pop sparkles
                for (angle in listOf(45.0, 135.0, 225.0, 315.0)) {
                    val rad = Math.toRadians(angle)
                    val sparkDist = radius * 1.06f
                    drawCircle(
                        color = Color(0xFFFF9800).copy(alpha = shockwaveAlpha.value),
                        radius = 2.5.dp.toPx() * (1f - shockwaveRadius.value * 0.4f),
                        center = Offset(
                            (centerX + sparkDist * cos(rad)).toFloat(),
                            (centerY + sparkDist * sin(rad)).toFloat()
                        )
                    )
                }
            }
        }

        // Mole clipping zone: Only shows above ground / hole rim
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(y = (-8).dp)
                .clipToBounds(),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (mole.state != MoleState.HIDDEN) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f)
                        .offset { IntOffset(0, (moleOffsetY.value * density).toInt()) }
                        .scale(
                            scaleX = moleScaleX.value * popScale.value,
                            scaleY = moleScaleY.value * popScale.value
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCuteMole(
                            size = size,
                            type = mole.type,
                            isHit = mole.state == MoleState.HIT
                        )
                    }

                    // If mole is hit, display dizzy stars & comic "¡PUM!" burst
                    if (mole.state == MoleState.HIT) {
                        HitEffectOverlay(starAngle = starAngle)
                    }
                }
            }
        }

        // Paws resting over hole bezel for 3D depth effect!
        if (mole.state == MoleState.UP || mole.state == MoleState.HIT) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .offset(y = (-8).dp)
            ) {
                if (moleOffsetY.value < 20f) {
                    drawMolePaws(size)
                }
            }
        }
    }
}

private fun DrawScope.drawHoleBezel(size: Size) {
    val width = size.width
    val height = size.height
    val centerX = width / 2f
    val centerY = height * 0.65f
    val radiusX = width * 0.44f
    val radiusY = height * 0.28f

    // Deep shadow under hole
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1B5E20), Color(0x00000000)),
            center = Offset(centerX, centerY + 12f),
            radius = radiusX * 1.15f
        ),
        topLeft = Offset(centerX - radiusX * 1.1f, centerY - radiusY * 0.8f + 12f),
        size = Size(radiusX * 2.2f, radiusY * 1.8f)
    )

    // Outer bright golden bezel
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEE58), Color(0xFFFFA000), Color(0xFFFF6F00))
        ),
        topLeft = Offset(centerX - radiusX, centerY - radiusY),
        size = Size(radiusX * 2f, radiusY * 2f)
    )

    // Inner rim stroke highlight
    drawOval(
        color = Color(0xFFFFF9C4),
        topLeft = Offset(centerX - radiusX + 3f, centerY - radiusY + 3f),
        size = Size(radiusX * 2f - 6f, radiusY * 2f - 6f),
        style = Stroke(width = 3f)
    )

    // Deep dark interior of the burrow/hole
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0F0803), Color(0xFF261405), Color(0xFF3E2723)),
            center = Offset(centerX, centerY + 4f),
            radius = radiusX * 0.85f
        ),
        topLeft = Offset(centerX - radiusX * 0.82f, centerY - radiusY * 0.72f),
        size = Size(radiusX * 1.64f, radiusY * 1.44f)
    )
}

private fun DrawScope.drawCuteMole(
    size: Size,
    type: MoleType,
    isHit: Boolean
) {
    val width = size.width
    val height = size.height
    val centerX = width / 2f
    val moleBaseY = height * 0.88f

    val bodyColor = when (type) {
        MoleType.DORADO -> Color(0xFFFFB300)
        else -> Color(0xFF8D6E63) // Rich warm chocolate mole fur
    }
    val bellyColor = when (type) {
        MoleType.DORADO -> Color(0xFFFFF176)
        else -> Color(0xFFD7CCC8) // Soft cream belly
    }

    // 1. Mole Body / Head (Dome shape)
    val bodyPath = Path().apply {
        moveTo(centerX - width * 0.35f, moleBaseY)
        cubicTo(
            centerX - width * 0.38f, height * 0.38f,
            centerX - width * 0.32f, height * 0.08f,
            centerX, height * 0.08f
        )
        cubicTo(
            centerX + width * 0.32f, height * 0.08f,
            centerX + width * 0.38f, height * 0.38f,
            centerX + width * 0.35f, moleBaseY
        )
        close()
    }
    drawPath(bodyPath, color = bodyColor)

    // Fur shade highlight
    drawOval(
        color = Color(0x22FFFFFF),
        topLeft = Offset(centerX - width * 0.16f, height * 0.12f),
        size = Size(width * 0.32f, height * 0.15f)
    )

    // 2. Cute Ears
    drawOval(
        color = bodyColor,
        topLeft = Offset(centerX - width * 0.36f, height * 0.18f),
        size = Size(width * 0.16f, height * 0.18f)
    )
    drawOval(
        color = Color(0xFFFFAB91),
        topLeft = Offset(centerX - width * 0.33f, height * 0.20f),
        size = Size(width * 0.10f, height * 0.12f)
    )

    drawOval(
        color = bodyColor,
        topLeft = Offset(centerX + width * 0.20f, height * 0.18f),
        size = Size(width * 0.16f, height * 0.18f)
    )
    drawOval(
        color = Color(0xFFFFAB91),
        topLeft = Offset(centerX + width * 0.23f, height * 0.20f),
        size = Size(width * 0.10f, height * 0.12f)
    )

    // 3. Snout & Cheeks (Soft oval)
    val snoutY = height * 0.42f
    drawOval(
        color = bellyColor,
        topLeft = Offset(centerX - width * 0.26f, snoutY),
        size = Size(width * 0.52f, height * 0.36f)
    )

    // Cheerful Pink Cheeks
    drawOval(
        color = Color(0x66FF5252),
        topLeft = Offset(centerX - width * 0.30f, snoutY + height * 0.08f),
        size = Size(width * 0.14f, height * 0.09f)
    )
    drawOval(
        color = Color(0x66FF5252),
        topLeft = Offset(centerX + width * 0.16f, snoutY + height * 0.08f),
        size = Size(width * 0.14f, height * 0.09f)
    )

    // 4. Eyes
    val eyeY = height * 0.34f
    val eyeSpacing = width * 0.13f

    if (isHit) {
        // Spiral dizzy eyes on hit (@_@)
        drawDizzyEye(Offset(centerX - eyeSpacing, eyeY), radius = width * 0.065f)
        drawDizzyEye(Offset(centerX + eyeSpacing, eyeY), radius = width * 0.065f)
    } else {
        // Bright friendly cartoon eyes with white light catch
        drawCircle(
            color = Color(0xFF212121),
            radius = width * 0.058f,
            center = Offset(centerX - eyeSpacing, eyeY)
        )
        drawCircle(
            color = Color.White,
            radius = width * 0.022f,
            center = Offset(centerX - eyeSpacing - width * 0.016f, eyeY - width * 0.016f)
        )

        drawCircle(
            color = Color(0xFF212121),
            radius = width * 0.058f,
            center = Offset(centerX + eyeSpacing, eyeY)
        )
        drawCircle(
            color = Color.White,
            radius = width * 0.022f,
            center = Offset(centerX + eyeSpacing - width * 0.016f, eyeY - width * 0.016f)
        )
    }

    // 5. Nose (Cute pink button nose)
    val noseY = height * 0.45f
    drawOval(
        color = Color(0xFFFF4081),
        topLeft = Offset(centerX - width * 0.08f, noseY),
        size = Size(width * 0.16f, height * 0.10f)
    )
    drawCircle(
        color = Color.White,
        radius = width * 0.02f,
        center = Offset(centerX - width * 0.02f, noseY + height * 0.025f)
    )

    // 6. Mouth / Tongue
    val mouthY = height * 0.58f
    if (isHit || type == MoleType.TRAVIESO) {
        // Dazed tongue sticking out
        drawOval(
            color = Color(0xFFFF1744),
            topLeft = Offset(centerX - width * 0.06f, mouthY),
            size = Size(width * 0.12f, height * 0.12f)
        )
    } else {
        // Sweet smile
        val smilePath = Path().apply {
            moveTo(centerX - width * 0.10f, mouthY)
            quadraticTo(centerX, mouthY + height * 0.06f, centerX + width * 0.10f, mouthY)
        }
        drawPath(smilePath, color = Color(0xFF3E2723), style = Stroke(width = 4f))
    }

    // 7. Whiskers
    drawLine(
        color = Color(0xFF4E342E),
        start = Offset(centerX - width * 0.14f, height * 0.50f),
        end = Offset(centerX - width * 0.32f, height * 0.47f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF4E342E),
        start = Offset(centerX - width * 0.14f, height * 0.54f),
        end = Offset(centerX - width * 0.32f, height * 0.55f),
        strokeWidth = 3f
    )

    drawLine(
        color = Color(0xFF4E342E),
        start = Offset(centerX + width * 0.14f, height * 0.50f),
        end = Offset(centerX + width * 0.32f, height * 0.47f),
        strokeWidth = 3f
    )
    drawLine(
        color = Color(0xFF4E342E),
        start = Offset(centerX + width * 0.14f, height * 0.54f),
        end = Offset(centerX + width * 0.32f, height * 0.55f),
        strokeWidth = 3f
    )

    // 8. Custom Accessories (Construction Helmet or Royal Crown)
    if (type == MoleType.CONSTRUCTOR) {
        drawConstructionHelmet(centerX, height * 0.12f, width)
    } else if (type == MoleType.DORADO) {
        drawKingCrown(centerX, height * 0.10f, width)
    }
}

private fun DrawScope.drawDizzyEye(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFF212121),
        radius = radius,
        center = center,
        style = Stroke(width = 3.5f)
    )
    drawCircle(
        color = Color(0xFFD50000),
        radius = radius * 0.5f,
        center = center,
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawConstructionHelmet(centerX: Float, baseCenterY: Float, width: Float) {
    val helmetWidth = width * 0.62f
    val helmetHeight = width * 0.36f

    // Dome of safety hardhat
    drawArc(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFFA000))
        ),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(centerX - helmetWidth * 0.46f, baseCenterY - helmetHeight * 0.65f),
        size = Size(helmetWidth * 0.92f, helmetHeight * 1.1f)
    )

    // Brim / visor
    drawRoundRect(
        color = Color(0xFFFFB300),
        topLeft = Offset(centerX - helmetWidth * 0.52f, baseCenterY - helmetHeight * 0.08f),
        size = Size(helmetWidth * 1.04f, helmetHeight * 0.28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Center ridge
    drawRoundRect(
        color = Color(0xFFFFF59D),
        topLeft = Offset(centerX - width * 0.06f, baseCenterY - helmetHeight * 0.60f),
        size = Size(width * 0.12f, helmetHeight * 0.65f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

private fun DrawScope.drawKingCrown(centerX: Float, baseCenterY: Float, width: Float) {
    val crownPath = Path().apply {
        moveTo(centerX - width * 0.24f, baseCenterY)
        lineTo(centerX - width * 0.24f, baseCenterY - width * 0.22f)
        lineTo(centerX - width * 0.12f, baseCenterY - width * 0.12f)
        lineTo(centerX, baseCenterY - width * 0.26f)
        lineTo(centerX + width * 0.12f, baseCenterY - width * 0.12f)
        lineTo(centerX + width * 0.24f, baseCenterY - width * 0.22f)
        lineTo(centerX + width * 0.24f, baseCenterY)
        close()
    }
    drawPath(
        crownPath,
        brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
    )
    drawCircle(color = Color(0xFFE91E63), radius = 5f, center = Offset(centerX, baseCenterY - width * 0.26f))
    drawCircle(color = Color(0xFF29B6F6), radius = 4f, center = Offset(centerX - width * 0.24f, baseCenterY - width * 0.22f))
    drawCircle(color = Color(0xFF29B6F6), radius = 4f, center = Offset(centerX + width * 0.24f, baseCenterY - width * 0.22f))
}

private fun DrawScope.drawMolePaws(size: Size) {
    val width = size.width
    val height = size.height
    val centerX = width / 2f
    val pawY = height * 0.68f

    // Left Paw
    drawOval(
        color = Color(0xFFFFCCBC),
        topLeft = Offset(centerX - width * 0.32f, pawY),
        size = Size(width * 0.16f, height * 0.11f)
    )
    // Right Paw
    drawOval(
        color = Color(0xFFFFCCBC),
        topLeft = Offset(centerX + width * 0.16f, pawY),
        size = Size(width * 0.16f, height * 0.11f)
    )
}

@Composable
private fun HitEffectOverlay(starAngle: Float) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Comic "¡PUM!" explosion badge
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF1744))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "¡PUM!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFEB3B)
            )
        }

        // Orbiting stars
        for (i in 0 until 4) {
            val angleRad = Math.toRadians((starAngle + i * 90).toDouble())
            val orbitRadius = 40.dp
            Text(
                text = "⭐",
                fontSize = 14.sp,
                modifier = Modifier.offset(
                    x = (orbitRadius.value * cos(angleRad)).dp,
                    y = (orbitRadius.value * sin(angleRad)).dp
                )
            )
        }
    }
}
