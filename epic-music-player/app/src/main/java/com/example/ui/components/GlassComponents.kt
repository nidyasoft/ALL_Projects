package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max
import com.example.ui.theme.*

/**
 * Reusable Glassmorphism style utility modifier.
 * Blends smooth semi-transparent surfaces with high-fidelity subtle borders.
 */
fun Modifier.glassy(
    backgroundColor: Color,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    shape: Shape
): Modifier = this
    .background(backgroundColor, shape)
    .border(1.dp, borderColor, shape)

/**
 * Reusable bouncy touch micro-interaction modifier.
 * Creates an elegant, organic squishy feedback scale-down and rebound when elements are tapped.
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.92f,
    onClick: () -> Unit
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    coroutineScope.launch {
                        scale.animateTo(scaleDown, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                    }
                    tryAwaitRelease()
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                },
                onTap = { onClick() }
            )
        }
}

/**
 * A stunning, premium kinetic typography text.
 * Triggers a spring-loaded letter-spacing drift and fade-in reveal transition whenever the content changes.
 */
@Composable
fun CharacterRevealText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1,
    key: Any
) {
    var animationTrigger by remember { mutableStateOf(0) }
    
    LaunchedEffect(key) {
        animationTrigger++
    }

    val progressByState = remember(animationTrigger) { Animatable(0f) }

    LaunchedEffect(progressByState) {
        progressByState.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val interpolatedSpacing = lerp(4f, 0.05f, progressByState.value)
    val interpolatedAlpha = lerp(0f, 1f, progressByState.value)

    Text(
        text = text,
        color = color.copy(alpha = interpolatedAlpha),
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        letterSpacing = interpolatedSpacing.sp,
        modifier = modifier
    )
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

/**
 * A highly performant dynamic background with drifting, blurred, glowing fluid color orbs.
 * These drift slowly across the screen using a single infinite transition to avoid frame drops.
 */
@Composable
fun FluidBackground(
    modifier: Modifier = Modifier,
    themeIndex: Int = 0,
    subThemeIndex: Int = 5,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FluidBackgroundTransition")
    
    // Slow drift animations (different periods to avoid repeating patterns)
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle3"
    )

    // Dynamic motion speed based on playback state
    val rotationSpeed = if (isPlaying) 1500 else 6000
    val timeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timeProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val backgroundColor = MaterialTheme.colorScheme.background
    
    // Remember leaf or star coordinates so we don't randomize on every recomposition
    val stars = remember {
        List(40) {
            Offset(
                x = (0..100).random() / 100f,
                y = (0..100).random() / 100f
            ) to ((5..20).random() / 10f)
        }
    }
    val leaves = remember {
        List(10) {
            Offset(
                x = (0..100).random() / 100f,
                y = (0..100).random() / 100f
            ) to ((3..12).random() / 10f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Draw solid base matching system theme background
        drawRect(color = backgroundColor)

        val w = size.width
        val h = size.height

        if (w > 0f && h > 0f) {
            val maxDim = max(w, h)

            when (themeIndex) {
                0 -> {
                    // Template 1: Legacy Fusion (All 6 previous themes wrapped)
                    val isCyberTheme = subThemeIndex == 4
                    if (isCyberTheme) {
                        // Retro Sunset Grid Theme!
                        val sunCenterY = h * 0.42f
                        val sunRadius = (w * 0.36f).coerceAtMost(220.dp.toPx())
                        
                        // Neon Sunset vertical gradient (Neon Pink to Hot Yellow)
                        val sunBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFFFFEA00)),
                            startY = sunCenterY - sunRadius,
                            endY = sunCenterY + sunRadius
                        )
                        
                        // Draw the classic synthwave sunset with sliced gaps
                        val numSlices = 7
                        for (i in 0 until numSlices) {
                            val startPct = i / numSlices.toFloat()
                            val endPct = (i + 1) / numSlices.toFloat()
                            
                            // Gradually make gaps larger at the bottom of the sun
                            val gapHeight = 5.dp.toPx() * (i + 1) / 3f
                            val sliceTop = sunCenterY - sunRadius + (sunRadius * 2f * startPct)
                            var sliceBottom = sunCenterY - sunRadius + (sunRadius * 2f * endPct)
                            if (i < numSlices - 1) {
                                sliceBottom -= gapHeight
                            }
                            
                            if (sliceBottom > sliceTop) {
                                val path = Path().apply {
                                    addRect(androidx.compose.ui.geometry.Rect(0f, sliceTop, w, sliceBottom))
                                }
                                drawContext.canvas.save()
                                drawContext.canvas.clipPath(path)
                                drawCircle(
                                    brush = sunBrush,
                                    radius = sunRadius,
                                    center = Offset(w / 2f, sunCenterY),
                                    alpha = 0.55f
                                )
                                drawContext.canvas.restore()
                            }
                        }
                        
                        // Draw glowing Neon Cyber Horizon Grid on lower part of screen
                        val horizonY = h * 0.62f
                        val gridColor = Color(0xFF00F3FF)
                        
                        // Draw horizontal perspective lines
                        val numHorizontalLines = 14
                        for (i in 0..numHorizontalLines) {
                            val t = i / numHorizontalLines.toFloat()
                            val y = horizonY + (h - horizonY) * (t * t)
                            val alpha = t * 0.35f
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = (1.5.dp.toPx() * t).coerceAtLeast(0.5f),
                                alpha = alpha
                            )
                        }
                        
                        // Draw vertical perspective rays radiating from horizon center
                        val numVerticalLines = 16
                        val vanishingPointX = w / 2f
                        for (i in 0..numVerticalLines) {
                            val t = i / numVerticalLines.toFloat()
                            val bottomX = w * t
                            val alpha = 0.32f
                            drawLine(
                                color = gridColor,
                                start = Offset(vanishingPointX + (bottomX - vanishingPointX) * 0.08f, horizonY),
                                end = Offset(bottomX, h),
                                strokeWidth = 1.dp.toPx(),
                                alpha = alpha
                            )
                        }
                    } else {
                        // Legacy fluid drift orbs
                        val cx1 = w / 2f + (w * 0.25f) * sin(angle1)
                        val cy1 = h / 2f + (h * 0.15f) * cos(angle1)
                        val radius1 = maxDim * 0.55f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.14f), Color.Transparent),
                                center = Offset(cx1, cy1),
                                radius = radius1
                            ),
                            center = Offset(cx1, cy1),
                            radius = radius1
                        )

                        val cx2 = w / 2f + (w * 0.2f) * cos(angle2)
                        val cy2 = h / 2f + (h * 0.22f) * sin(angle2 * 2f)
                        val radius2 = maxDim * 0.45f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(secondaryColor.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(cx2, cy2),
                                radius = radius2
                            ),
                            center = Offset(cx2, cy2),
                            radius = radius2
                        )

                        val cx3 = w / 2f + (w * 0.18f) * sin(angle3 + 1f)
                        val cy3 = h / 2f + (h * 0.2f) * cos(angle3 - 1f)
                        val radius3 = maxDim * 0.4f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(tertiaryColor.copy(alpha = 0.10f), Color.Transparent),
                                center = Offset(cx3, cy3),
                                radius = radius3
                            ),
                            center = Offset(cx3, cy3),
                            radius = radius3
                        )
                    }
                }
                1 -> {
                    // Template 2: Glassmorphic Aurora (Dynamic wavy energy ribbons)
                    for (ribbon in 0..2) {
                        val phaseOffset = ribbon * (Math.PI / 3f).toFloat()
                        val path = Path()
                        
                        val steps = 18
                        for (step in 0..steps) {
                            val fraction = step / steps.toFloat()
                            val rx = w * fraction
                            val sineVal = sin(fraction * Math.PI * 2.5f + timeProgress * 2f * Math.PI + phaseOffset).toFloat()
                            val ry = h * (0.28f + ribbon * 0.09f) + (h * 0.06f) * sineVal
                            if (step == 0) {
                                path.moveTo(rx, ry)
                            } else {
                                path.lineTo(rx, ry)
                            }
                        }
                        
                        val ribbonColor = when (ribbon) {
                            0 -> primaryColor
                            1 -> secondaryColor
                            else -> Color(0xFF00FFCC)
                        }
                        
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, ribbonColor.copy(alpha = 0.18f), ribbonColor.copy(alpha = 0.32f), Color.Transparent)
                            ),
                            style = Stroke(width = 54.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    
                    // Soft ambient background orbs
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(w * 0.15f, h * 0.25f),
                            radius = w * 0.55f
                        ),
                        center = Offset(w * 0.15f, h * 0.25f),
                        radius = w * 0.55f
                    )
                }
                2 -> {
                    // Template 3: Retro Tape Deck
                    val tapeCenterY = h * 0.45f
                    val tapeWidth = (w * 0.72f).coerceAtMost(320.dp.toPx())
                    val tapeHeight = tapeWidth * 0.62f
                    val tapeLeft = w / 2f - tapeWidth / 2f
                    val tapeTop = tapeCenterY - tapeHeight / 2f
                    
                    // Outer cassette пластик structure
                    drawRoundRect(
                        color = TapePrimary.copy(alpha = 0.22f),
                        topLeft = Offset(tapeLeft, tapeTop),
                        size = androidx.compose.ui.geometry.Size(tapeWidth, tapeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Transparent tape window
                    val winWidth = tapeWidth * 0.56f
                    val winHeight = tapeHeight * 0.36f
                    val winLeft = w / 2f - winWidth / 2f
                    val winTop = tapeCenterY - winHeight / 2f
                    drawRect(
                        color = Color.Black.copy(alpha = 0.45f),
                        topLeft = Offset(winLeft, winTop),
                        size = androidx.compose.ui.geometry.Size(winWidth, winHeight)
                    )
                    
                    val gearRadius = winHeight * 0.33f
                    val leftGearX = w / 2f - winWidth * 0.28f
                    val rightGearX = w / 2f + winWidth * 0.28f
                    
                    // Left and right rotating tape roll layers
                    val leftRoll = gearRadius + (15.dp.toPx() * (1f - timeProgress * 0.45f))
                    val rightRoll = gearRadius + (15.dp.toPx() * (0.55f + timeProgress * 0.45f))
                    
                    drawCircle(
                        color = Color(0xFF6B4C35),
                        radius = leftRoll,
                        center = Offset(leftGearX, tapeCenterY),
                        alpha = 0.75f
                    )
                    drawCircle(
                        color = Color(0xFF6B4C35),
                        radius = rightRoll,
                        center = Offset(rightGearX, tapeCenterY),
                        alpha = 0.75f
                    )
                    
                    // Left sprocket
                    val angleRotation = timeProgress * 360f
                    drawContext.canvas.save()
                    drawContext.canvas.translate(leftGearX, tapeCenterY)
                    drawContext.canvas.rotate(angleRotation)
                    drawCircle(color = TapePrimary, radius = gearRadius)
                    for (spoke in 0..5) {
                        val ang = (spoke * (Math.PI / 3f)).toFloat()
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, 0f),
                            end = Offset(gearRadius * cos(ang), gearRadius * sin(ang)),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    drawCircle(color = Color.Black, radius = gearRadius * 0.38f)
                    drawContext.canvas.restore()
                    
                    // Right sprocket
                    drawContext.canvas.save()
                    drawContext.canvas.translate(rightGearX, tapeCenterY)
                    drawContext.canvas.rotate(angleRotation)
                    drawCircle(color = TapePrimary, radius = gearRadius)
                    for (spoke in 0..5) {
                        val ang = (spoke * (Math.PI / 3f)).toFloat()
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, 0f),
                            end = Offset(gearRadius * cos(ang), gearRadius * sin(ang)),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    drawCircle(color = Color.Black, radius = gearRadius * 0.38f)
                    drawContext.canvas.restore()
                    
                    // Static retro vintage grid overlay lines
                    val lineSpace = 6.dp.toPx()
                    val lineNum = (h / lineSpace).toInt()
                    for (i in 0..lineNum) {
                        val ly = i * lineSpace
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, ly),
                            end = Offset(w, ly),
                            strokeWidth = 1.dp.toPx(),
                            alpha = 0.15f
                        )
                    }
                }
                3 -> {
                    // Template 4: Cyber Grid Neon (Fast Scrolling Vertical Grid)
                    val horizonY = h * 0.5f
                    val gridColor = GridPrimary.copy(alpha = 0.35f)
                    
                    val numHorizontalLines = 15
                    for (i in 0..numHorizontalLines) {
                        val t = (i + timeProgress) / numHorizontalLines.toFloat()
                        val y = horizonY + (h - horizonY) * (t * t)
                        val alpha = t * 0.55f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = (2.2.dp.toPx() * t).coerceAtLeast(0.6f),
                            alpha = alpha
                        )
                    }
                    
                    val numVerticalLines = 14
                    val vanishingPointX = w / 2f
                    for (i in 0..numVerticalLines) {
                        val t = i / numVerticalLines.toFloat()
                        val bottomX = w * t
                        drawLine(
                            color = gridColor,
                            start = Offset(vanishingPointX + (bottomX - vanishingPointX) * 0.05f, horizonY),
                            end = Offset(bottomX, h),
                            strokeWidth = 1.2.dp.toPx(),
                            alpha = 0.28f
                        )
                    }
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GridSecondary.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(w / 2f, horizonY - 35.dp.toPx()),
                            radius = w * 0.3f
                        ),
                        center = Offset(w / 2f, horizonY - 35.dp.toPx()),
                        radius = w * 0.3f
                    )
                }
                4 -> {
                    // Template 5: Zen Nature Ripple
                    val numRipples = 3
                    for (i in 0 until numRipples) {
                        val phase = (timeProgress + i / numRipples.toFloat()) % 1f
                        val rRadius = (w * 0.65f) * phase
                        val rAlpha = (1f - phase) * 0.26f
                        
                        drawCircle(
                            color = ZenPrimary,
                            radius = rRadius,
                            center = Offset(w / 2f, h * 0.48f),
                            style = Stroke(width = 1.5.dp.toPx()),
                            alpha = rAlpha
                        )
                    }
                    
                    leaves.forEach { (pos, speed) ->
                        val ix = pos.x * w
                        val iy = pos.y * h
                        val y = (iy + h * timeProgress * speed) % h
                        val x = ix + 24.dp.toPx() * sin(timeProgress * 2f * Math.PI.toFloat() + pos.y * 10f)
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ZenSecondary.copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(x, y),
                                radius = 22.dp.toPx()
                            ),
                            center = Offset(x, y),
                            radius = 22.dp.toPx()
                        )
                    }
                }
                5 -> {
                    // Template 6: Cosmic Starfield
                    val rx = w / 2f
                    val ry = h * 0.4f
                    val rRadius = w * 0.38f
                    
                    drawContext.canvas.save()
                    drawContext.canvas.translate(rx, ry)
                    drawContext.canvas.skew(-0.3f, 0.12f)
                    
                    drawCircle(
                        color = StarPrimary,
                        radius = rRadius,
                        style = Stroke(width = 3.dp.toPx()),
                        alpha = 0.3f
                    )
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(StarSecondary.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        radius = rRadius * 0.62f
                    )
                    drawContext.canvas.restore()
                    
                    stars.forEach { (pos, sparkle) ->
                        val sx = pos.x * w
                        val sy = pos.y * h
                        val alphaVal = (0.22f + 0.78f * sin(timeProgress * 2 * Math.PI.toFloat() * sparkle + pos.x * 6f).coerceIn(-1f, 1f).let { (it + 1) / 2f }) * 0.75f
                        
                        drawCircle(
                            color = Color.White,
                            radius = (1.5.dp.toPx() + 0.8.dp.toPx() * sparkle),
                            center = Offset(sx, sy),
                            alpha = alphaVal
                        )
                    }
                }
            }
        }
    }
}

/**
 * Representing particles in the visualizer.
 */
private data class DriftingParticle(
    val initialAngle: Float,
    val speed: Float,
    val baseRadius: Float,
    val size: Float,
    val opacity: Float
)

/**
 * A highly advanced Canvas-based Audio Wave and Particle Visualizer.
 * Animates beautiful multi-frequency curved waves and organic radiating particle stars.
 * Coordinates naturally with whether playback is active (isPlaying):
 *   - When isPlaying = true: Waves fluctuate vibrantly and stars float quickly.
 *   - When isPlaying = false: Waves decrease to a flat, gentle, ambient vibration, and stars slow down.
 */
@Composable
fun AdvancedAudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTransition")
    
    // Wave animation phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3500 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Particle/Star movement multiplier
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePhase"
    )

    // Dynamic wave amplitude scale depending on playing state
    val amplitudeMultiplier by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.08f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "amplitude"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    // Generate a set of 24 randomized aesthetic ambient stars once
    val particles = remember {
        List(24) { i ->
            DriftingParticle(
                initialAngle = (i * (360f / 24f) * (Math.PI / 180f)).toFloat(),
                speed = 1.2f + (i % 4) * 0.5f,
                baseRadius = 120f + (i % 3) * 35f,
                size = 3f + (i % 3) * 2f,
                opacity = 0.3f + (i % 5) * 0.15f
            )
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        if (w > 0f && h > 0f) {
            // ------------------ 1. Drawing Glowing Energy Waves around Center ------------------
            val outerPulse = cx * 0.82f + 12f * sin(wavePhase * 2f) * amplitudeMultiplier
            drawCircle(
                color = primaryColor.copy(alpha = 0.04f * amplitudeMultiplier),
                center = Offset(cx, cy),
                radius = outerPulse + 24f
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.05f * amplitudeMultiplier),
                center = Offset(cx, cy),
                radius = outerPulse + 8f
            )

            // Inner circle heartbeat reference line
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                center = Offset(cx, cy),
                radius = cx * 0.76f,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // ------------------ 2. Organic Concentric Particle Starfield ------------------
            particles.forEach { particle ->
                // Calculate expanded radius with drift offset
                val driftOffset = (particlePhase * particle.speed) % 150f
                val currentRadius = (particle.baseRadius + driftOffset) * (cx / 190f)
                
                // Fade out particles near the edges
                val pathProgress = driftOffset / 150f
                val currentOpacity = particle.opacity * (1f - pathProgress) * (if (isPlaying) 1.0f else 0.3f)

                if (currentRadius < cx) {
                    val angle = particle.initialAngle + (wavePhase * 0.1f * particle.speed)
                    val px = cx + currentRadius * cos(angle)
                    val py = cy + currentRadius * sin(angle)

                    drawCircle(
                        color = secondaryColor.copy(alpha = currentOpacity),
                        center = Offset(px, py),
                        radius = particle.size.dp.toPx()
                    )
                }
            }

            // ------------------ 3. Circular Wave Modulation Ring (Pulsing Equator) ------------------
            val ringPoints = 72
            val ringPath = Path()
            val baseRingRadius = cx * 0.76f

            for (i in 0..ringPoints) {
                val angle = (i * (360f / ringPoints) * (Math.PI / 180f)).toFloat()
                
                // Generates wave ripples on specific frequency phases
                val freqOffset = sin((angle * 6f + wavePhase * 3f).toDouble()).toFloat() * 14f * amplitudeMultiplier
                val freqOffsetSub = cos((angle * 12f - wavePhase * 1.5f).toDouble()).toFloat() * 6f * amplitudeMultiplier
                
                val r = baseRingRadius + freqOffset + freqOffsetSub
                val px = cx + r * cos(angle)
                val py = cy + r * sin(angle)

                if (i == 0) {
                    ringPath.moveTo(px, py)
                } else {
                    ringPath.lineTo(px, py)
                }
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = primaryColor.copy(alpha = 0.55f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Second outer echo ring path
            val ringPathEcho = Path()
            val baseRingRadiusEcho = cx * 0.76f
            for (i in 0..ringPoints) {
                val angle = (i * (360f / ringPoints) * (Math.PI / 180f)).toFloat()
                val freqOffset = cos((angle * 5f - wavePhase * 2.2f).toDouble()).toFloat() * 12f * amplitudeMultiplier
                val r = baseRingRadiusEcho + freqOffset
                val px = cx + r * cos(angle)
                val py = cy + r * sin(angle)

                if (i == 0) {
                    ringPathEcho.moveTo(px, py)
                } else {
                    ringPathEcho.lineTo(px, py)
                }
            }
            ringPathEcho.close()
            drawPath(
                path = ringPathEcho,
                color = tertiaryColor.copy(alpha = 0.3f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

/**
 * Vintage Gramophone Record & Tonearm Visualizer ("صفحه گرامافون قدیمی")
 * A beautifully rendered antique vinyl turntable with spinning record and animated tonearm.
 */
@Composable
fun VintageGramophoneVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GramophoneRotation")
    
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3200 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )

    val tonearmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 26f else -4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tonearmAngle"
    )

    val isLightMode = MaterialTheme.colorScheme.background.red > 0.5f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (cx * 0.88f).coerceAtMost(cy * 0.88f)

            if (radius > 0f) {
                // 1. Turntable Base Platter
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isLightMode) Color(0xFFD7C29E) else Color(0xFF3B281B),
                            if (isLightMode) Color(0xFFB8860B) else Color(0xFF1F120A)
                        ),
                        center = Offset(cx, cy),
                        radius = radius * 1.08f
                    ),
                    center = Offset(cx, cy),
                    radius = radius * 1.08f
                )
                // Platter Metallic Rim
                drawCircle(
                    color = if (isLightMode) Color(0xFFB8860B) else Color(0xFFD4AF37),
                    center = Offset(cx, cy),
                    radius = radius * 1.03f,
                    style = Stroke(width = 4.dp.toPx())
                )

                // 2. Vinyl Record (Rotates continuously)
                rotate(degrees = rotationDegrees, pivot = Offset(cx, cy)) {
                    // Vinyl Surface
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF262626),
                                Color(0xFF141414),
                                Color(0xFF080808)
                            ),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        center = Offset(cx, cy),
                        radius = radius
                    )

                    // Vinyl Micro Grooves
                    val numGrooves = 14
                    for (i in 1..numGrooves) {
                        val r = radius * (0.35f + (i / numGrooves.toFloat()) * 0.62f)
                        val alpha = if (i % 3 == 0) 0.35f else 0.18f
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            center = Offset(cx, cy),
                            radius = r,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Vinyl Sheen / Reflection Cone
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        startAngle = -45f,
                        sweepAngle = 50f,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(cx - radius, cy - radius)
                    )
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = radius
                        ),
                        startAngle = 135f,
                        sweepAngle = 50f,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(cx - radius, cy - radius)
                    )

                    // Center Record Label (Antique Gold/Parchment)
                    val labelRadius = radius * 0.34f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isLightMode) Color(0xFFFAF0E6) else Color(0xFFC5A059),
                                if (isLightMode) Color(0xFFE6D2B5) else Color(0xFF8B6508)
                            )
                        ),
                        center = Offset(cx, cy),
                        radius = labelRadius
                    )
                    drawCircle(
                        color = if (isLightMode) Color(0xFF6B4226) else Color(0xFFD4AF37),
                        center = Offset(cx, cy),
                        radius = labelRadius,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Center Spindle Hole
                    drawCircle(
                        color = Color.Black,
                        center = Offset(cx, cy),
                        radius = labelRadius * 0.12f
                    )
                    drawCircle(
                        color = Color.LightGray,
                        center = Offset(cx, cy),
                        radius = labelRadius * 0.07f
                    )
                }

                // 3. Antique Gramophone Needle Tonearm Assembly
                val pivotX = cx + radius * 0.85f
                val pivotY = cy - radius * 0.85f

                rotate(degrees = tonearmAngle, pivot = Offset(pivotX, pivotY)) {
                    // Pivot Base
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFF8B6508))
                        ),
                        center = Offset(pivotX, pivotY),
                        radius = 16.dp.toPx()
                    )

                    // Curved Metallic Arm
                    val armPath = Path().apply {
                        moveTo(pivotX, pivotY)
                        cubicTo(
                            pivotX - radius * 0.3f, pivotY + radius * 0.3f,
                            pivotX - radius * 0.5f, pivotY + radius * 0.6f,
                            cx + radius * 0.15f, cy - radius * 0.1f
                        )
                    }
                    drawPath(
                        path = armPath,
                        color = if (isLightMode) Color(0xFFB8860B) else Color(0xFFE6C280),
                        style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    // Needle Head resting on vinyl
                    val needleX = cx + radius * 0.15f
                    val needleY = cy - radius * 0.1f
                    drawCircle(
                        color = Color(0xFFD4AF37),
                        center = Offset(needleX, needleY),
                        radius = 7.dp.toPx()
                    )
                    drawCircle(
                        color = Color.Red,
                        center = Offset(needleX, needleY),
                        radius = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
