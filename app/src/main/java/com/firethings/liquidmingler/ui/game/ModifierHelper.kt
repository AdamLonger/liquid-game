package com.firethings.liquidmingler.ui.game

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.firethings.liquidmingler.state.BucketUpdateType
import kotlin.math.floor

fun Modifier.bucketZModifier(
    layer: BucketLayer,
    updateType: BucketUpdateType
): Modifier {
    val z =  when (updateType) {
        is BucketUpdateType.Pour -> when (layer) {
            BucketLayer.STREAM -> layer.zIndexMultiplier
            BucketLayer.BACKGROUND,
            BucketLayer.LIQUID,
            BucketLayer.FOREGROUND -> MAX_Z_INDEX
        }
        BucketUpdateType.Fill -> layer.zIndexMultiplier
        BucketUpdateType.None -> layer.zIndexMultiplier
    }

    return zIndex(z)
}

fun Modifier.layoutBucket(
    visuals: BucketVisuals,
    bucketIndex: Int,
    itemPerRow: Int,
): Modifier {
    val x = (visuals.size.width + BucketHorizontalSpacing) * (bucketIndex % itemPerRow)
    val y = (visuals.size.height + BucketVerticalSpacing) * floor(bucketIndex / itemPerRow.toFloat())
    return padding(start = x, top = y)
}

fun Modifier.applyBucketLayout(
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
        translationY = if (!hasFinishedAnimation) data.translationY else 0f
        transformOrigin = TransformOrigin(
            pivotFractionX = 0.5f + bendCenterOffsetPercent * if (data.pourMultiplier < 0) -1f else 1f,
            pivotFractionY = 0f,
        )
        rotationZ = if (!hasFinishedAnimation) bendLevel * BucketRotateExtent else 0f
        scaleX = 1f
        scaleY = 1f
    }
}

fun <V : BucketVisuals> Modifier.applyStreamLayout(
    withLayout: BucketVisualsWithLayout<V>
): Modifier = (withLayout.layoutData as? BucketUpdateLayoutData.Pour)?.let { data ->
    this.graphicsLayer {
        translationX = data.pourTranslationX
        translationY = data.pourTranslationY
    }
} ?: this


