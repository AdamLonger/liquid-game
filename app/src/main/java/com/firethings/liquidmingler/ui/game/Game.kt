package com.firethings.liquidmingler.ui.game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.firethings.liquidmingler.state.Bucket
import com.firethings.liquidmingler.state.BucketUpdate
import com.firethings.liquidmingler.state.BucketUpdateType
import com.firethings.liquidmingler.state.GameState
import com.firethings.liquidmingler.state.Liquid
import com.firethings.liquidmingler.state.Scene
import com.firethings.liquidmingler.state.SceneAction
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

val startScene = Scene(
    buckets = listOf(
        Bucket(0, 3, listOf()),
        Bucket(1, 3, listOf()),
        Bucket(2, 3, listOf(Liquid.Red, Liquid.Blue)),
    )
)

@Composable
fun Game(scene: Scene) {
    val scope = rememberCoroutineScope()
    val state: GameState by scene.state.collectAsState()
    val layoutCoordinates = remember(scene) { mutableMapOf<Int, LayoutCoordinates>() }

    val configuration = LocalConfiguration.current
    val screenHeight = remember { configuration.screenHeightDp.dp }
    val screenWidth = remember { configuration.screenWidthDp.dp }
    val itemPerRow = remember { floor((screenWidth - 40.dp) / (BucketWidth + BucketHorizontalSpacing)).toInt() }
    val startInset =
        remember { (screenWidth - BucketWidth * itemPerRow - BucketHorizontalSpacing * (itemPerRow - 1)) / 2f }

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
                state.updates.forEachIndexed { index, update ->
                    //Background
                    Box(
                        modifier = Modifier
                            .layoutBucket(index, itemPerRow)
                            .bucketZModifier(update.updateType is BucketUpdateType.Pour, state.buckets.size)
                    ) {
                        AnimatedBucketBackgroundComponent(
                            width = BucketWidth,
                            height = BucketHeight,
                            update = BucketUpdateWithLayout.wrap(update, layoutCoordinates),
                            onLayout = { layoutCoordinates[update.bucketId] = it }
                        )
                    }

                    //Stream
                    Box(
                        modifier = Modifier
                            .layoutBucket(index, itemPerRow)
                            .bucketZModifier(update.updateType is BucketUpdateType.Pour, state.buckets.size)
                    ) {
                        AnimatedStreamComponent(
                            width = BucketWidth,
                            height = BucketHeight,
                            update = BucketUpdateWithLayout.wrap(update, layoutCoordinates)
                        )
                    }

                    //Liquids
                    Box(
                        modifier = Modifier
                            .layoutBucket(index, itemPerRow)
                            .bucketZModifier(update.updateType is BucketUpdateType.Pour, state.buckets.size)
                    ) {
                        AnimatedLiquidComponent(
                            width = BucketWidth,
                            height = BucketHeight,
                            update = BucketUpdateWithLayout.wrap(update, layoutCoordinates)
                        )
                    }

                    //Foreground
                    Box(
                        modifier = Modifier
                            .layoutBucket(index, itemPerRow)
                            .bucketZModifier(update.updateType is BucketUpdateType.Pour, state.buckets.size)
                    ) {
                        AnimatedBucketForegroundComponent(
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
}


fun Modifier.layoutBucket(
    bucketIndex: Int,
    itemPerRow: Int,
): Modifier {
    val x = (BucketWidth + BucketHorizontalSpacing) * (bucketIndex % itemPerRow)
    val y = (BucketHeight + BucketVerticalSpacing) * floor(bucketIndex / itemPerRow.toFloat())
    return padding(start = x, top = y)
}

@Composable
fun onEachRow(
    updates: List<BucketUpdate>,
    compose: @Composable RowScope.(BucketUpdate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        updates.forEach { update -> compose(update) }
    }
}