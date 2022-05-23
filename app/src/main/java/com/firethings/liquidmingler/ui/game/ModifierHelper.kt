package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex

fun Modifier.bucketZModifier(active: Boolean, bucketSize: Int): Modifier =
    this.zIndex(if (active) (bucketSize + 1) * 4f else 0f)


fun Modifier.applyLayoutData(
    hasFinishedAnimation: Boolean,
    data: BucketUpdateLayoutData,
    bendLevel: Float
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
            pivotFractionX = if (data.bendMultiplier < 0) 0f else 1f,
            pivotFractionY = 0f,
        )
        rotationZ = if (!hasFinishedAnimation) bendLevel * BucketRotateExtent else 0f
        scaleX = 1f
        scaleY = 1f
    }
}
