package com.firethings.liquidmingler.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Composable
fun <V: BucketVisuals> AnimatedStreamComponent(
    update: BucketVisualsWithLayout<V>
) {
    val animationProgress by animateFloatAsState(
        targetValue = update.current.content.size.toFloat(),
        animationSpec = tween(durationMillis = TransitionDuration)
    )
    val hasFinishedAnimation = floor(animationProgress) == animationProgress

    if (!hasFinishedAnimation && update.current.content.size < update.previous.content.size) {
        Canvas(
            modifier = Modifier
                .width(BucketPourWidth)
                .height(update.visuals.size.height)
                .offset(if (update.bendMultiplier < 0) 0.dp else update.visuals.size.width + BucketPourWidth)
        ) {
            val heightPx = (BucketPourOffset + update.visuals.size.height).toPx()
            val streamHeightPx = BucketPourOffset.toPx()
            val streamWidthPx = BucketPourWidth.toPx()
            val path = Path()

            path.apply {
                if (update.bendMultiplier < 0) {
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

            drawPath(path, update.previous.topPortion.firstOrNull()?.color() ?: Color.Transparent)
        }
    }
}
