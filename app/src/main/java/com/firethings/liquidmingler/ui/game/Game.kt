package com.firethings.liquidmingler.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.firethings.liquidmingler.state.Bucket
import com.firethings.liquidmingler.state.BucketUpdateType
import com.firethings.liquidmingler.state.GameState
import com.firethings.liquidmingler.state.Liquid
import com.firethings.liquidmingler.state.Scene
import com.firethings.liquidmingler.state.SceneAction
import kotlinx.coroutines.launch

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