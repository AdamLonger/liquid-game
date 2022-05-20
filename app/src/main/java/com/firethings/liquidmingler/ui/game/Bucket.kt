package com.firethings.liquidmingler.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.state.Bucket
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min


@Composable
fun AnimatedBucketComponent(
    width: Dp,
    height: Dp,
    update: BucketUpdateWithLayout,
    onLayout: (LayoutCoordinates) -> Unit,
    onClick: (bucket: Bucket) -> Unit = {}
) {
    val animationProgress by animateFloatAsState(
        targetValue = update.current.content.size.toFloat(),
        animationSpec = tween(durationMillis = 4000)
    )
    val hasFinishedAnimation = floor(animationProgress) == animationProgress
    val animatingContent =
        (if (update.previous.content.size >= update.current.content.size) update.previous else update.current)
            .content.map { it.color() }

    val bendLevel =
        if (hasFinishedAnimation || update.previous.content.size <= update.current.content.size) 0f
        else (1f - animationProgress / update.current.size.toFloat()) * update.bendMultiplier
    val liquidLevel = animationProgress / update.current.size.toFloat()

    Box(modifier = Modifier
        .wrapContentSize()
        .onGloballyPositioned(onLayout)
        .applyLayoutData(hasFinishedAnimation, update.layoutData, bendLevel)
        .clickable { if (hasFinishedAnimation) onClick.invoke(update.current) }
    ) {
        BucketComponent(
            width = width,
            height = height,
            size = update.current.size,
            content = animatingContent,
            bendLevel = bendLevel,
            bendRight = update.bendMultiplier > 0,
            liquidLevel = liquidLevel,
        )
    }
}

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

@Composable
fun BucketComponent(
    width: Dp,
    height: Dp,
    size: Int,
    content: List<Color>,
    bendLevel: Float = 0f,
    bendRight: Boolean = true,
    liquidLevel: Float = 1f,
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(Color.LightGray)
    ) {
        val widthPx = width.toPx()
        val heightPx = height.toPx()

        clipRect(0f, 0f, widthPx, heightPx) {
            drawLiquids(
                widthPx = widthPx,
                heightPx = heightPx,
                liquidHeightPx = heightPx / size.toFloat(),
                animatedHeightPx = liquidLevel * heightPx,
                bendAngle = abs(bendLevel) * BucketRotateExtent,
                bendRight = bendRight,
                content = content
            )
        }
    }
}

fun DrawScope.drawLiquids(
    widthPx: Float,
    heightPx: Float,
    liquidHeightPx: Float,
    animatedHeightPx: Float,
    bendAngle: Float,
    bendRight: Boolean = true,
    content: List<Color>,
) {
    content.forEachIndexed { index, color ->
        val shape = calculateLiquidShape(
            width = widthPx,
            height = min((content.size - index) * liquidHeightPx, animatedHeightPx),
            angle = bendAngle
        )

        val path = Path()

        when (shape) {
            is LiquidShape.Trapeze -> {
                val leftSide = if (bendRight) shape.shorterSize else shape.longerSide
                val rightSide = if (bendRight) shape.longerSide else shape.shorterSize

                path.moveTo(0f, heightPx - leftSide)
                path.lineTo(0f, heightPx)
                path.lineTo(widthPx, heightPx)
                path.lineTo(widthPx, heightPx - rightSide)
                path.close()
            }
            is LiquidShape.Triangle -> {
                if (bendRight) {
                    path.moveTo(widthPx - shape.adjacentSize, heightPx)
                    path.lineTo(widthPx, heightPx)
                    path.lineTo(widthPx, heightPx - shape.oppositeSize)
                    path.close()
                } else {
                    path.moveTo(shape.adjacentSize, heightPx)
                    path.lineTo(0f, heightPx)
                    path.lineTo(0f, heightPx - shape.oppositeSize)
                    path.close()
                }
            }
        }

        drawPath(path, color)
    }
}