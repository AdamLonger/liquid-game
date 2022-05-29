package com.firethings.liquidmingler.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.firethings.liquidmingler.state.Bucket
import kotlin.math.floor

@Composable
fun <V : BucketVisuals> AnimatedGameComponent(
    withLayout: BucketVisualsWithLayout<V>,
    onLayout: (LayoutCoordinates) -> Unit = {},
    onClick: (bucket: Bucket) -> Unit = {},
    component: @Composable (
        content: List<Color>,
        bendLevel: Float,
        liquidLevel: Float,
    ) -> Unit
) {
    val animationProgress by animateFloatAsState(
        targetValue = withLayout.current.content.size.toFloat(),
        animationSpec = tween(durationMillis = TransitionDuration)
    )
    val hasFinishedAnimation = floor(animationProgress) == animationProgress
    val animatingContent =
        (if (withLayout.previous.content.size >= withLayout.current.content.size) withLayout.previous else withLayout.current)
            .content.map { it.color() }

    val bendLevel =
        if (hasFinishedAnimation || withLayout.layoutData !is BucketUpdateLayoutData.Pour) 0f
        else (1f - animationProgress / withLayout.current.volume.toFloat()) * withLayout.layoutData.pourMultiplier
    val liquidLevel = animationProgress / withLayout.current.volume.toFloat()

    Box(modifier = Modifier
        .wrapContentSize()
        .onGloballyPositioned(onLayout)
        .applyBucketLayout(
            hasFinishedAnimation, withLayout.layoutData, bendLevel,
            bendCenterOffsetPercent = withLayout.visuals.bendCenterOffsetPercent
        )
        .clickable { if (hasFinishedAnimation) onClick.invoke(withLayout.current) }
    ) {
        component(
            content = animatingContent,
            bendLevel = bendLevel,
            liquidLevel = liquidLevel,
        )
    }
}
