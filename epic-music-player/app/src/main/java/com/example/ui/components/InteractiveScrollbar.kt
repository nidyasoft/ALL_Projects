package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive Auto-Hiding Fast Scrollbar for LazyColumn
 * - Becomes visible upon scrolling or dragging
 * - Remains visible for 3 seconds after scrolling stops
 * - Smoothly fades out
 * - Touch & drag enabled for ultra-fast scrubbing/scrolling through large lists
 */
@Composable
fun FastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    thumbColorPrimary: Color = MaterialTheme.colorScheme.primary,
    thumbColorSecondary: Color = Color(0xFF38BDF8),
    autoHideDelayMs: Long = 3000L
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val isScrollInProgress = state.isScrollInProgress

    // Auto-hide timer: stays visible while scrolling/dragging, plus 3 seconds after
    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            isVisible = true
        } else {
            delay(autoHideDelayMs)
            isVisible = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "ScrollbarAlpha"
    )

    if (alpha <= 0.01f && !isVisible) {
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .alpha(alpha),
        contentAlignment = Alignment.TopEnd
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        val totalItems = state.layoutInfo.totalItemsCount
        val visibleItems = state.layoutInfo.visibleItemsInfo.size

        if (totalItems <= 1 || trackHeightPx <= 0f) return@BoxWithConstraints

        val density = LocalDensity.current
        val minThumbHeightPx = with(density) { 44.dp.toPx() }
        val maxThumbHeightPx = trackHeightPx * 0.4f

        val ratio = visibleItems.toFloat() / totalItems.toFloat()
        val thumbHeightPx = (trackHeightPx * ratio).coerceIn(minThumbHeightPx, maxThumbHeightPx)
        val availableTrackPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(1f)

        // Calculate current scroll fraction
        val firstIndex = state.firstVisibleItemIndex
        val firstOffset = state.firstVisibleItemScrollOffset
        val itemSize = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.coerceAtLeast(1) ?: 1
        val maxScrollIndex = (totalItems - visibleItems).coerceAtLeast(1).toFloat()

        val scrollFraction = ((firstIndex.toFloat() + firstOffset.toFloat() / itemSize) / maxScrollIndex).coerceIn(0f, 1f)
        val thumbYPx = (scrollFraction * availableTrackPx).coerceIn(0f, availableTrackPx)
        val thumbYDp = with(density) { thumbYPx.toDp() }
        val thumbHeightDp = with(density) { thumbHeightPx.toDp() }

        // Full interactive track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalItems, availableTrackPx, thumbHeightPx) {
                    detectTapGestures { offset ->
                        val targetY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, availableTrackPx)
                        val frac = targetY / availableTrackPx
                        val targetIndex = (frac * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                        coroutineScope.launch {
                            state.scrollToItem(targetIndex)
                        }
                    }
                }
                .pointerInput(totalItems, availableTrackPx, thumbHeightPx) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val targetY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, availableTrackPx)
                            val frac = targetY / availableTrackPx
                            val targetIndex = (frac * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                            coroutineScope.launch {
                                state.scrollToItem(targetIndex)
                            }
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val targetY = (change.position.y - thumbHeightPx / 2f).coerceIn(0f, availableTrackPx)
                            val frac = targetY / availableTrackPx
                            val targetIndex = (frac * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                            coroutineScope.launch {
                                state.scrollToItem(targetIndex)
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    )
                }
        ) {
            // Subtle track guideline
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(2.dp))
            )

            // Fast scroll draggable thumb
            Box(
                modifier = Modifier
                    .offset(y = thumbYDp)
                    .padding(end = 3.dp)
                    .width(if (isDragging) 8.dp else 5.dp)
                    .height(thumbHeightDp)
                    .align(Alignment.TopEnd)
                    .shadow(
                        elevation = if (isDragging) 8.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = thumbColorPrimary
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(thumbColorSecondary, thumbColorPrimary)
                        )
                    )
            )
        }
    }
}

/**
 * Interactive Auto-Hiding Fast Scrollbar for standard ScrollState (non-lazy column)
 */
@Composable
fun FastScrollbarForScrollState(
    state: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
    thumbColorPrimary: Color = MaterialTheme.colorScheme.primary,
    thumbColorSecondary: Color = Color(0xFF38BDF8),
    autoHideDelayMs: Long = 3000L
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    val isScrollInProgress = state.isScrollInProgress

    LaunchedEffect(isScrollInProgress, isDragging) {
        if (isScrollInProgress || isDragging) {
            isVisible = true
        } else {
            delay(autoHideDelayMs)
            isVisible = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "ScrollbarAlphaForScroll"
    )

    if (alpha <= 0.01f && !isVisible) {
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(28.dp)
            .alpha(alpha),
        contentAlignment = Alignment.TopEnd
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        val maxValue = state.maxValue

        if (maxValue <= 0 || trackHeightPx <= 0f) return@BoxWithConstraints

        val density = LocalDensity.current
        val minThumbHeightPx = with(density) { 40.dp.toPx() }
        val thumbHeightPx = (trackHeightPx * 0.25f).coerceAtLeast(minThumbHeightPx)
        val availableTrackPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(1f)

        val scrollFraction = (state.value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        val thumbYPx = (scrollFraction * availableTrackPx).coerceIn(0f, availableTrackPx)
        val thumbYDp = with(density) { thumbYPx.toDp() }
        val thumbHeightDp = with(density) { thumbHeightPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(maxValue, availableTrackPx, thumbHeightPx) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val targetY = (offset.y - thumbHeightPx / 2f).coerceIn(0f, availableTrackPx)
                            val frac = targetY / availableTrackPx
                            coroutineScope.launch {
                                state.scrollTo((frac * maxValue).toInt().coerceIn(0, maxValue))
                            }
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val targetY = (change.position.y - thumbHeightPx / 2f).coerceIn(0f, availableTrackPx)
                            val frac = targetY / availableTrackPx
                            coroutineScope.launch {
                                state.scrollTo((frac * maxValue).toInt().coerceIn(0, maxValue))
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .offset(y = thumbYDp)
                    .padding(end = 3.dp)
                    .width(if (isDragging) 8.dp else 5.dp)
                    .height(thumbHeightDp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(thumbColorSecondary, thumbColorPrimary)
                        )
                    )
            )
        }
    }
}
