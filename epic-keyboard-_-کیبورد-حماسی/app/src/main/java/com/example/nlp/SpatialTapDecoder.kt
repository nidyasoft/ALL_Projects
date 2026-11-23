package com.example.nlp

import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max

data class KeyHitRect(
    val id: String,
    val char: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(1f)
    val height: Float get() = (bottom - top).coerceAtLeast(1f)
    val centerX: Float get() = (left + right) * 0.5f
    val centerY: Float get() = (top + bottom) * 0.5f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }
}

/**
 * Google Gboard-inspired Bayesian Spatial + Language Model Tap Decoder.
 *
 * Handles:
 * 1. Gap / Between-Keys Touch Disambiguation: When a user taps on the gap/margin between two keys,
 *    resolves the tap to the best key using Gaussian spatial likelihood + language-model priors.
 * 2. Fat-Finger Edge Correction: When a tap lands near the border of a key adjacent to a much higher
 *    probability next character in context, adjusts to the intended key cleanly.
 * 3. Dynamic Key Hit-Target Bias: Likely next characters automatically gain wider effective touch
 *    confidence near borders without disturbing deliberate center taps.
 */
object SpatialTapDecoder {

    fun decodeTap(
        tapX: Float,
        tapY: Float,
        directKeyId: String?,
        directKeyChar: String?,
        keyRects: List<KeyHitRect>,
        nextCharProbs: Map<Char, Float>,
        hasContextOrWord: Boolean,
        smartTouchEnabled: Boolean
    ): String? {
        if (!smartTouchEnabled || keyRects.isEmpty()) {
            return directKeyChar
        }

        val directRect = if (directKeyId != null) {
            keyRects.firstOrNull { it.id == directKeyId }
        } else {
            keyRects.firstOrNull { it.contains(tapX, tapY) }
        }

        val avgWidth = keyRects.map { it.width }.average().toFloat().coerceAtLeast(36f)
        val avgHeight = keyRects.map { it.height }.average().toFloat().coerceAtLeast(36f)
        val searchRadius = max(avgWidth * 1.35f, avgHeight * 1.25f)

        // Find nearby character keys sorted by normalized spatial distance
        val nearby = keyRects.filter { rect ->
            hypot(tapX - rect.centerX, tapY - rect.centerY) <= searchRadius
        }

        if (nearby.isEmpty()) {
            return directKeyChar
        }

        // Case 1: Touch landed in the GAP / inter-key spacing (not strictly inside any key rect)
        if (directRect == null) {
            return pickBestBayesianCandidate(
                tapX = tapX,
                tapY = tapY,
                candidates = nearby,
                nextCharProbs = nextCharProbs,
                lmWeight = if (hasContextOrWord) 0.55 else 0.15
            )?.char
        }

        val directOutput = directKeyChar ?: directRect.char

        // Normalized radial offset from key center (0.0 = dead center, 1.0 = corner/edge)
        val normDx = kotlin.math.abs(tapX - directRect.centerX) / (directRect.width * 0.5f)
        val normDy = kotlin.math.abs(tapY - directRect.centerY) / (directRect.height * 0.5f)
        val normEdge = max(normDx, normDy).coerceIn(0f, 1.2f)

        // Strict center hit guard: very centered taps (< 0.36) stay deterministic unless the user
        // clearly fat-fingered an adjacent key that has near-zero LM plausibility vs an overwhelming neighbor.
        if (normEdge < 0.34f) {
            return directOutput
        }

        val directCh = directOutput.firstOrNull()?.lowercaseChar()
        val directProb = if (directCh != null) (nextCharProbs[directCh] ?: 0f) else 0f

        // Smoothly scale LM weight from 0.25 (inner zone) up to 0.78 (boundary zone)
        val lmWeight = if (!hasContextOrWord) {
            0.22
        } else {
            (0.24 + ((normEdge - 0.34f) / 0.66f) * 0.54).coerceIn(0.24, 0.80)
        }

        val directScore = scoreCandidate(tapX, tapY, directRect, directProb, lmWeight)

        var bestRect = directRect
        var bestScore = directScore

        for (candidate in nearby) {
            if (candidate.id == directRect.id) continue
            val candDist = hypot(tapX - candidate.centerX, tapY - candidate.centerY)
            // Only consider close neighboring keys (fat-finger reach)
            if (candDist > max(directRect.width * 1.10f, directRect.height * 1.10f)) continue

            val candCh = candidate.char.firstOrNull()?.lowercaseChar()
            val candProb = if (candCh != null) (nextCharProbs[candCh] ?: 0f) else 0f

            val candScore = scoreCandidate(tapX, tapY, candidate, candProb, lmWeight)

            // Require a meaningful Bayesian margin to override a key whose bounds actually contain the tap:
            // - Larger margin near inner zone, smaller margin right on the border
            val requiredMargin = when {
                normEdge >= 0.72f -> 0.18
                normEdge >= 0.50f -> 0.42
                else -> 0.75
            }

            // Never override if direct key already has decent LM support (> 0.12) unless almost on the edge
            val directSupported = directProb >= 0.12f && normEdge < 0.72f
            if (!directSupported && candScore > bestScore + requiredMargin) {
                bestScore = candScore
                bestRect = candidate
            }
        }

        return bestRect?.char ?: directOutput
    }

    private fun pickBestBayesianCandidate(
        tapX: Float,
        tapY: Float,
        candidates: List<KeyHitRect>,
        nextCharProbs: Map<Char, Float>,
        lmWeight: Double
    ): KeyHitRect? {
        var best: KeyHitRect? = null
        var bestScore = Double.NEGATIVE_INFINITY
        for (rect in candidates) {
            val ch = rect.char.firstOrNull()?.lowercaseChar()
            val prob = if (ch != null) (nextCharProbs[ch] ?: 0f) else 0f
            val score = scoreCandidate(tapX, tapY, rect, prob, lmWeight)
            if (score > bestScore) {
                bestScore = score
                best = rect
            }
        }
        return best
    }

    private fun scoreCandidate(
        tapX: Float,
        tapY: Float,
        rect: KeyHitRect,
        lmProb: Float,
        lmWeight: Double
    ): Double {
        val sigmaX = (rect.width * 0.52f).coerceAtLeast(16f)
        val sigmaY = (rect.height * 0.58f).coerceAtLeast(18f)
        val dx = (tapX - rect.centerX) / sigmaX
        val dy = (tapY - rect.centerY) / sigmaY
        val spatialLogLik = -0.5 * (dx * dx + dy * dy)

        // Floor epsilon keeps unseen characters typable while strongly favoring likely next characters
        val lmLogPrior = ln(0.03 + lmProb.toDouble() * 3.5)
        return spatialLogLik + lmWeight * lmLogPrior
    }
}
