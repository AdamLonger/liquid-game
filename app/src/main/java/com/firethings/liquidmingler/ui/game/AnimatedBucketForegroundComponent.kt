package com.firethings.liquidmingler.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.R
import com.firethings.liquidmingler.state.Bucket

@Composable
fun AnimatedBucketForegroundComponent(
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
    BucketForegroundComponent(
        width = width,
        height = height,
    )
}

@Composable
fun BucketForegroundComponent(
    width: Dp,
    height: Dp
) = Image(
    modifier = Modifier
        .width(width)
        .height(height),
    painter = painterResource(id = R.drawable.img_bottle_front),
    contentScale = ContentScale.FillBounds,
    contentDescription = null
)
