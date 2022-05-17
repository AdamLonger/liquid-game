package com.firethings.liquidmingler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.firethings.liquidmingler.ui.theme.LiquidMinglerTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiquidMinglerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    GameComponent(startScene)
                }
            }
        }
    }
}

val startScene = Scene(
    buckets = listOf(
        Bucket(0, 3, listOf()),
        Bucket(1, 3, listOf(Liquid.Red, Liquid.Red, Liquid.Red)),
        Bucket(2, 3, listOf(Liquid.Blue, Liquid.Blue, Liquid.Blue)),
    )
)

@Composable
fun GameComponent(scene: Scene) {
    val scope = rememberCoroutineScope()
    val state: GameState by scene.state.collectAsState()
    val layoutCoordinates = remember(scene) { mutableMapOf<Int, LayoutCoordinates>() }

    LaunchedEffect(scene) {
        scene.startEventProcessing(scope)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Icon(
            Icons.Rounded.Refresh,
            contentDescription = "Reset game",
            modifier = Modifier
                .padding(20.dp)
                .size(40.dp)
                .align(Alignment.TopEnd)
                .clickable {
                    scope.launch {
                        scene.actionChannel.send(SceneAction.Reset)
                    }
                }
        )

        if (state.buckets.all { it.isComplete }) {
            Text(
                "Completed!",
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.updates.forEach { update ->
                Box(
                    modifier = Modifier
                        .zIndex(if (update.updateType is BucketUpdateType.Pour) 100f else 0f)
                        .padding(horizontal = BucketSpacing / 2f)
                        .wrapContentSize()
                ) {
                    AnimatedBucketComponent(
                        width = BucketWidth,
                        height = BucketHeight,
                        update = BucketUpdateWithLayout.wrap(update, layoutCoordinates),
                        onLayout = { layoutCoordinates[update.bucketId] = it }
                    ) { clickedBucket ->
                        scope.launch {
                            scene.actionChannel.send(SceneAction.BucketTap(clickedBucket.id))
                        }
                    }
                }
            }
        }
    }
}

sealed class BucketUpdateLayoutData {
    abstract val bendMultiplier: Float
    abstract val isSelected: Boolean
    val scale: Float get() = if (isSelected) 1.3f else 1f

    data class None(override val isSelected: Boolean = false) : BucketUpdateLayoutData() {
        override val bendMultiplier = 1f
    }

    data class Fill(override val isSelected: Boolean = false) : BucketUpdateLayoutData() {
        override val bendMultiplier = 1f
    }

    data class Pour(
        override val bendMultiplier: Float,
        override val isSelected: Boolean,
        val translationX: Float,
        val translationY: Float,
    ) : BucketUpdateLayoutData()
}


data class BucketUpdateWithLayout(
    val update: BucketUpdate,
    val layoutData: BucketUpdateLayoutData
) {
    val bucketId get() = update.bucketId
    val current get() = update.current
    val previous get() = update.previous
    val updateType get() = update.updateType
    val bendMultiplier get() = layoutData.bendMultiplier
    val isSelected get() = update.isSelected

    companion object {
        fun wrap(update: BucketUpdate, layoutMap: Map<Int, LayoutCoordinates>): BucketUpdateWithLayout {
            return when (update.updateType) {
                BucketUpdateType.Fill ->
                    BucketUpdateWithLayout(update, BucketUpdateLayoutData.Fill(isSelected = update.isSelected))
                BucketUpdateType.None ->
                    BucketUpdateWithLayout(update, BucketUpdateLayoutData.None(isSelected = update.isSelected))
                is BucketUpdateType.Pour -> {
                    val fromPos = layoutMap.getValue(update.bucketId)
                    val toPos = layoutMap.getValue(update.updateType.destinationId)
                    val fromBounds = fromPos.boundsInRoot()
                    val toBounds = toPos.boundsInRoot()

                    val bendMultiplier = if (fromBounds.left <= toBounds.left) 1f else -1f
                    val translationX = toBounds.left - fromBounds.left - bendMultiplier * fromPos.size.width
                    val translationY = toBounds.top - fromBounds.top

                    BucketUpdateWithLayout(
                        update, BucketUpdateLayoutData.Pour(
                            bendMultiplier = bendMultiplier,
                            isSelected = update.isSelected,
                            translationX = translationX,
                            translationY = translationY
                        )
                    )
                }
            }
        }
    }
}

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
        animationSpec = tween(durationMillis = 800)
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
        translationY = if (!hasFinishedAnimation) data.translationY else 0f
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
            rotate(
                -bendLevel * BucketRotateExtent,
                pivot = Offset(widthPx, heightPx)
            ) {
                val transformedHeight = widthPx + (heightPx - widthPx) * (1f - abs(bendLevel))
                val sectionHeight = transformedHeight / size.toFloat()
                val extendedWidth = sqrt(widthPx.pow(2) + heightPx.pow(2)) + widthPx

                clipRect(
                    0f, heightPx * (1f - liquidLevel), extendedWidth, heightPx,
                    clipOp = ClipOp.Intersect
                ) {
                    content.reversed().forEachIndexed { index, color ->
                        drawRect(
                            color = color,
                            size = Size(extendedWidth, sectionHeight),
                            topLeft = Offset(0f, heightPx - sectionHeight * (index + 1))
                        )
                    }
                }
            }
        }
    }
}

fun Liquid.color() = when (this) {
    Liquid.Blue -> Color.Blue
    Liquid.Red -> Color.Red
}

private val BucketWidth = 40.dp
private val BucketHeight = 100.dp
private val BucketSpacing = 50.dp
private const val BucketRotateExtent = 90f
