package com.firethings.liquidmingler

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.firethings.liquidmingler.state.Bucket
import com.firethings.liquidmingler.state.Bucket.*
import com.firethings.liquidmingler.state.GameState
import com.firethings.liquidmingler.state.Liquid
import com.firethings.liquidmingler.state.Scene
import com.firethings.liquidmingler.state.SceneAction
import com.firethings.liquidmingler.ui.game.AnimatedBucketBackgroundComponent
import com.firethings.liquidmingler.ui.game.AnimatedBucketForegroundComponent
import com.firethings.liquidmingler.ui.game.AnimatedLiquidComponent
import com.firethings.liquidmingler.ui.game.AnimatedStreamComponent
import com.firethings.liquidmingler.ui.game.BucketHorizontalSpacing
import com.firethings.liquidmingler.ui.game.BucketPourOffset
import com.firethings.liquidmingler.ui.game.BucketPourWidth
import com.firethings.liquidmingler.ui.game.BucketUpdateLayoutData
import com.firethings.liquidmingler.ui.game.BucketVisualsWithLayout
import com.firethings.liquidmingler.ui.game.BucketVerticalSpacing
import com.firethings.liquidmingler.ui.game.BucketVisuals
import com.firethings.liquidmingler.ui.game.bucketZModifier
import kotlinx.coroutines.launch
import kotlin.math.floor

val startScene = Scene(
    buckets = listOf(
        Bucket(0, 3, listOf()),
        Bucket(1, 3, listOf(Liquid.Red, Liquid.Blue)),
        Bucket(2, 3, listOf()),
        Bucket(3, 3, listOf()),
        Bucket(4, 3, listOf(Liquid.Red, Liquid.Red, Liquid.Red)),
        Bucket(5, 3, listOf()),
        Bucket(6, 3, listOf()),
        Bucket(7, 3, listOf()),
        Bucket(8, 3, listOf()),
    )
)

@Composable
fun Game(scene: Scene) {
    val scope = rememberCoroutineScope()
    val state: GameState by scene.state.collectAsState()
    val layoutCoordinates = remember(scene) { mutableMapOf<Int, LayoutCoordinates>() }

    val configuration = LocalConfiguration.current
    val screenWidth = remember { configuration.screenWidthDp.dp }
    val itemPerRow = remember { 3 /*floor((screenWidth - 40.dp) / (BucketWidth + BucketHorizontalSpacing)).toInt() */ }

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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //Background
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                state.updates.map { BucketVisuals.Flask(it) }.forEachIndexed { index, visuals ->
                    val withLayout = BucketVisualsWithLayout.wrap(visuals, layoutCoordinates,
                        with(LocalDensity.current) { screenWidth.toPx() },
                        with(LocalDensity.current) { BucketPourWidth.toPx() }
                    )

                    //Background
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                visuals.updateType,
                                state.buckets.size
                            )
                    ) {
                        AnimatedBucketBackgroundComponent(
                            withLayout = withLayout,
                            onLayout = { layoutCoordinates[visuals.bucketId] = it }
                        )
                    }

                    //Stream
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                visuals.updateType,
                                state.buckets.size
                            )
                            .let { mod ->
                                (withLayout.layoutData as? BucketUpdateLayoutData.Pour)?.let { data ->
                                    mod.graphicsLayer {
                                        translationX = data.translationX +
                                                (-BucketPourWidth.toPx() - visuals.size.width.toPx()) / 2f
                                        translationY = data.translationY - BucketPourOffset.toPx()
                                    }
                                } ?: mod
                            }
                    ) { AnimatedStreamComponent(update = withLayout) }

                    //Liquids
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                visuals.updateType,
                                state.buckets.size,
                                true
                            )
                    ) {
                        AnimatedLiquidComponent(withLayout = withLayout)
                    }

                    //Foreground
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                visuals.updateType,
                                state.buckets.size,
                                true
                            )
                    ) {
                        AnimatedBucketForegroundComponent(
                            withLayout = withLayout,
                            onLayout = { layoutCoordinates[visuals.bucketId] = it }
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
