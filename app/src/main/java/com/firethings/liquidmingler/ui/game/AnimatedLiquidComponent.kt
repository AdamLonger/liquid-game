package com.firethings.liquidmingler.ui.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorSpace
import android.view.View
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSupported
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import com.firethings.liquidmingler.R
import com.firethings.liquidmingler.state.Bucket
import kotlin.math.abs
import kotlin.math.min

@Composable
fun AnimatedLiquidComponent(
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
    LiquidComponent(
        width = width,
        height = height,
        size = update.current.size,
        content = content,
        bendLevel = bendLevel,
        bendRight = update.bendMultiplier > 0,
        liquidLevel = liquidLevel,
    )
}

@Composable
fun LiquidComponent(
    width: Dp,
    height: Dp,
    size: Int,
    content: List<Color>,
    bendLevel: Float = 0f,
    bendRight: Boolean = true,
    liquidLevel: Float = 1f,
) {
    val widthPx = with(LocalDensity.current) { width.toPx() }
    val heightPx = with(LocalDensity.current) { height.toPx() }
    val copyBitmap = remember { ImageBitmap(widthPx.toInt(), heightPx.toInt()) }
    val copyCanvas = remember { androidx.compose.ui.graphics.Canvas(copyBitmap) }

    val image = ImageVector.vectorResource(id = R.drawable.img_liquid)
    val painter = rememberVectorPainter(defaultWidth = width,
        defaultHeight = height,
        viewportWidth = image.viewportWidth,
        viewportHeight = image.viewportHeight,
        name = image.name,
        autoMirror = false, content = { _, _ -> RenderVectorGroup(group = image.root) }
    )

    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
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
                liquidHeightPx = heightPx / size.toFloat(),
                animatedHeightPx = liquidLevel * heightPx,
                bendAngle = abs(bendLevel) * BucketRotateExtent,
                bendRight = bendRight,
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

        drawPath(path, Paint().apply { this.color = color })
    }
}