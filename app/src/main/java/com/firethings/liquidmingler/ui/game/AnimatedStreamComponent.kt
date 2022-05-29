package com.firethings.liquidmingler.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.abs
import kotlin.math.floor

@Composable
fun <V : BucketVisuals> AnimatedStreamComponent(
    withLayout: BucketVisualsWithLayout<V>
) {
    val animationProgress by animateFloatAsState(
        targetValue = withLayout.current.size.toFloat(),
        animationSpec = tween(
            durationMillis = abs(withLayout.current.size - withLayout.previous.size) * VolumeTweenDuration
        )
    )
    val hasFinishedAnimation = floor(animationProgress) == animationProgress

    if (!hasFinishedAnimation && withLayout.current.size < withLayout.previous.size) {
        Canvas(
            modifier = Modifier
                .width(BucketPourWidth)
                .height(withLayout.visuals.size.height)
                .applyStreamLayout(withLayout)

        ) {
            val heightPx = (BucketPourOffset + withLayout.visuals.size.height).toPx()
            val streamHeightPx = BucketPourOffset.toPx()
            val streamWidthPx = BucketPourWidth.toPx()
            val path = Path()

            val pourMultiplier = (withLayout.layoutData as? BucketUpdateLayoutData.Pour)?.pourMultiplier ?: 0f
            path.apply {
                if (pourMultiplier < 0) {
                    moveTo(streamWidthPx, 0f)
                    lineTo(streamWidthPx, heightPx)
                    lineTo(0f, heightPx)
                    lineTo(0f, streamHeightPx)
                    close()
                } else {
                    moveTo(streamWidthPx - streamWidthPx, 0f)
                    lineTo(streamWidthPx - streamWidthPx, heightPx)
                    lineTo(streamWidthPx, heightPx)
                    lineTo(streamWidthPx, streamHeightPx)
                    close()
                }

            }

            drawPath(
                path,
                withLayout.previous.topPortion.firstOrNull()?.color()?.copy(alpha = 0.5f) ?: Color.Transparent
            )
        }
    }
}
