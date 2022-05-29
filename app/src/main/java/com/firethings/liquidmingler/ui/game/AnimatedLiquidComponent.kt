package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.R
import com.firethings.liquidmingler.state.Bucket
import kotlin.math.abs
import kotlin.math.min

@Composable
fun <V : BucketVisuals> AnimatedLiquidComponent(
    withLayout: BucketVisualsWithLayout<V>,
    onLayout: (LayoutCoordinates) -> Unit = {},
    onClick: (bucket: Bucket) -> Unit = {}
) = AnimatedGameComponent(
    withLayout,
    onLayout,
    onClick
) { content, bendLevel, liquidLevel ->
    LiquidComponent(
        withLayout = withLayout,
        content = content,
        bendLevel = bendLevel,
        liquidLevel = liquidLevel,
    )
}

@Composable
fun <V: BucketVisuals> LiquidComponent(
    withLayout: BucketVisualsWithLayout<V>,
    content: List<Color>,
    bendLevel: Float = 0f,
    liquidLevel: Float = 1f,
) {
    val widthPx = with(LocalDensity.current) {withLayout.visuals.size.width.toPx() }
    val heightPx = with(LocalDensity.current) { withLayout.visuals.size.height.toPx() }
    val copyBitmap = remember { ImageBitmap(widthPx.toInt(), heightPx.toInt()) }
    val copyCanvas = remember { androidx.compose.ui.graphics.Canvas(copyBitmap) }

    val image = ImageVector.vectorResource(id = withLayout.visuals.liquidRes)
    val painter = rememberVectorPainter(
        defaultWidth = withLayout.visuals.size.width,
        defaultHeight = withLayout.visuals.size.height,
        viewportWidth = image.viewportWidth,
        viewportHeight = image.viewportHeight,
        name = image.name,
        autoMirror = false, content = { _, _ -> RenderVectorGroup(group = image.root) }
    )

    Canvas(
        modifier = Modifier
            .width(withLayout.visuals.size.width)
            .height(withLayout.visuals.size.height)
            .alpha(0.99f)
    ) {
        copyCanvas.drawRect(0f, 0f, widthPx, heightPx, paint = Paint().apply {
            color = Color.Transparent
            blendMode = BlendMode.Src
        })
        clipRect(0f, 0f, widthPx, heightPx) {
            with(painter) {
                draw(Size(widthPx, heightPx))
            }

            copyCanvas.drawLiquid(
                widthPx = widthPx,
                heightPx = heightPx,
                liquidHeightPx = heightPx / withLayout.visuals.update.current.volume.toFloat(),
                animatedHeightPx = liquidLevel * heightPx,
                bendAngle = abs(bendLevel) * BucketRotateExtent,
                pourDirection = (withLayout.layoutData as? BucketUpdateLayoutData.Pour)?.pourDirection,
                content = content
            )

            copyBitmap.prepareToDraw()

            drawIntoCanvas {
                it.drawImage(
                    copyBitmap,
                    Offset.Zero,
                    paint = Paint().apply {
                        blendMode = BlendMode.SrcIn
                    })
            }
        }
    }
}

private fun androidx.compose.ui.graphics.Canvas.drawLiquid(
    widthPx: Float,
    heightPx: Float,
    liquidHeightPx: Float,
    animatedHeightPx: Float,
    bendAngle: Float,
    pourDirection: PourDirection?,
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
                val (leftSide, rightSide) = when(pourDirection){
                    PourDirection.LEFT, null -> shape.shorterSize to shape.longerSide
                    PourDirection.RIGHT-> shape.longerSide to shape.shorterSize
                }

                path.moveTo(0f, heightPx - leftSide)
                path.lineTo(0f, heightPx)
                path.lineTo(widthPx, heightPx)
                path.lineTo(widthPx, heightPx - rightSide)
                path.close()
            }
            is LiquidShape.Triangle -> {
                when (pourDirection){
                    PourDirection.LEFT, null -> {
                        path.moveTo(widthPx - shape.adjacentSize, heightPx)
                        path.lineTo(widthPx, heightPx)
                        path.lineTo(widthPx, heightPx - shape.oppositeSize)
                        path.close()
                    }
                    PourDirection.RIGHT -> {
                        path.moveTo(shape.adjacentSize, heightPx)
                        path.lineTo(0f, heightPx)
                        path.lineTo(0f, heightPx - shape.oppositeSize)
                        path.close()
                    }
                }
            }
        }

        drawPath(path, Paint().apply { this.color = color })
    }
}