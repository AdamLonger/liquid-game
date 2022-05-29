package com.firethings.liquidmingler.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.R
import com.firethings.liquidmingler.state.Bucket

@Composable
fun <V : BucketVisuals> AnimatedBucketBackgroundComponent(
    withLayout: BucketVisualsWithLayout<V>,
    onLayout: (LayoutCoordinates) -> Unit,
    onClick: (bucket: Bucket) -> Unit = {}
) = AnimatedGameComponent(
    withLayout,
    onLayout,
    onClick
) { _, _, _ ->
    BucketBackgroundComponent(withLayout.visuals)
}

@Composable
fun BucketBackgroundComponent(
    visuals: BucketVisuals
) = Image(
    modifier = Modifier.size(visuals.size),
    painter = painterResource(id = visuals.backRes),
    contentScale = ContentScale.FillBounds,
    contentDescription = null
)
