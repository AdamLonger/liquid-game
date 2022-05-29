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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.firethings.liquidmingler.state.Bucket
import com.firethings.liquidmingler.state.GameState
import com.firethings.liquidmingler.state.Liquid
import com.firethings.liquidmingler.state.Scene
import com.firethings.liquidmingler.state.SceneAction
import com.firethings.liquidmingler.ui.game.AnimatedBucketBackgroundComponent
import com.firethings.liquidmingler.ui.game.AnimatedBucketForegroundComponent
import com.firethings.liquidmingler.ui.game.AnimatedLiquidComponent
import com.firethings.liquidmingler.ui.game.AnimatedStreamComponent
import com.firethings.liquidmingler.ui.game.BucketLayer
import com.firethings.liquidmingler.ui.game.BucketPourOffset
import com.firethings.liquidmingler.ui.game.BucketPourWidth
import com.firethings.liquidmingler.ui.game.BucketVisualsWithLayout
import com.firethings.liquidmingler.ui.game.BucketVisuals
import com.firethings.liquidmingler.ui.game.applyStreamLayout
import com.firethings.liquidmingler.ui.game.bucketZModifier
import com.firethings.liquidmingler.ui.game.layoutBucket
import kotlinx.coroutines.launch

val startScene = Scene(
    buckets = listOf(
        Bucket(0, 3, listOf()),
        Bucket(1, 3, listOf(Liquid.Red, Liquid.Red, Liquid.Red)),
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
                        with(LocalDensity.current) { BucketPourWidth.toPx() },
                        with(LocalDensity.current) { BucketPourOffset.toPx() }
                    )

                    //Background
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                BucketLayer.BACKGROUND,
                                visuals.updateType
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
                                BucketLayer.STREAM,
                                visuals.updateType
                            )
                    ) { AnimatedStreamComponent(withLayout = withLayout) }

                    //Liquids
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                BucketLayer.LIQUID,
                                visuals.updateType
                            )
                    ) {
                        AnimatedLiquidComponent(withLayout = withLayout)
                    }

                    //Foreground
                    Box(
                        modifier = Modifier
                            .layoutBucket(visuals, index, itemPerRow)
                            .bucketZModifier(
                                BucketLayer.FOREGROUND,
                                visuals.updateType
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

