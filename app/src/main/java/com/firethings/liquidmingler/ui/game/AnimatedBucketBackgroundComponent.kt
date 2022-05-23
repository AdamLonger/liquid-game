package com.firethings.liquidmingler.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.state.Bucket

@Composable
fun AnimatedBucketBackgroundComponent(
    width: Dp,
    height: Dp,
    update: BucketUpdateWithLayout,
    onLayout: (LayoutCoordinates) -> Unit,
    onClick: (bucket: Bucket) -> Unit = {}
) = AnimatedGameComponent(
    update,
    onLayout,
    onClick
) { _, _, _ ->
    BucketBackgroundComponent(
        width = width,
        height = height,
    )
}

@Composable
fun BucketBackgroundComponent(
    width: Dp,
    height: Dp
) = Box(
    modifier = Modifier
        .width(width)
        .height(height)
        .background(Color.LightGray)
)
