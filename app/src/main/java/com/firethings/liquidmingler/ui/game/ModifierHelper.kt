package com.firethings.liquidmingler.ui.game

import androidx.annotation.FloatRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.firethings.liquidmingler.state.BucketUpdateType

fun Modifier.bucketZModifier(
    updateType: BucketUpdateType,
    bucketSize: Int,
    bringForeground: Boolean = false
): Modifier = zIndex(
    when (updateType) {
        is BucketUpdateType.Pour -> (bucketSize + 1) * 4f
        BucketUpdateType.Fill -> (bucketSize + 1) * if (bringForeground) 1000f else 0f
        BucketUpdateType.None -> 0f
    }
)

fun Modifier.applyLayoutData(
    hasFinishedAnimation: Boolean,
    data: BucketUpdateLayoutData,
    bendLevel: Float,
    @FloatRange(from = 0.0, to = 0.5)
    bendCenterOffsetPercent: Float = 0.5f,
) = when (data) {
    is BucketUpdateLayoutData.Fill, is BucketUpdateLayoutData.None -> graphicsLayer {
        transformOrigin = TransformOrigin(
            pivotFractionX = 0.5f,
            pivotFractionY = 0.5f,
        )
        scaleX = data.scale
        scaleY = data.scale
    }
    is BucketUpdateLayoutData.Pour -> graphicsLayer {
        translationX = if (!hasFinishedAnimation) data.translationX else 0f
        translationY = if (!hasFinishedAnimation) data.translationY - BucketPourOffset.toPx() else 0f
        transformOrigin = TransformOrigin(
            pivotFractionX = 0.5f + bendCenterOffsetPercent * if (data.bendMultiplier < 0) -1f else 1f,
            pivotFractionY = 0f,
        )
        rotationZ = if (!hasFinishedAnimation) bendLevel * BucketRotateExtent else 0f
        scaleX = 1f
        scaleY = 1f
    }
}
