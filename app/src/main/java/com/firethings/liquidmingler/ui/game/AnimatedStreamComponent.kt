package com.firethings.liquidmingler.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.firethings.liquidmingler.state.Bucket
import kotlin.math.abs
import kotlin.math.min

@Composable
fun AnimatedStreamComponent(
    width: Dp,
    height: Dp,
    update: BucketUpdateWithLayout,
    onLayout: (LayoutCoordinates) -> Unit = {},
    onClick: (bucket: Bucket) -> Unit = {}
) = AnimatedGameComponent(
    update,
    onLayout,
    onClick
) { content, bendLevel, liquidLevel ->
    Box(modifier = Modifier.width(40.dp).height(40.dp).background(Color.Magenta)) {
    }
}

@Composable
fun StreamComponent(
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
    ) {
        val widthPx = width.toPx()
        val heightPx = height.toPx()

        drawStream(
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

fun DrawScope.drawStream(
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