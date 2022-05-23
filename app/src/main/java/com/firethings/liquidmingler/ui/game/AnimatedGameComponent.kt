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
fun AnimatedGameComponent(
    update: BucketUpdateWithLayout,
    onLayout: (LayoutCoordinates) -> Unit = {},
    onClick: (bucket: Bucket) -> Unit = {},
    component: @Composable (
        content: List<Color>,
        bendLevel: Float,
        liquidLevel: Float,
    ) -> Unit
) {
    val animationProgress by animateFloatAsState(
        targetValue = update.current.content.size.toFloat(),
        animationSpec = tween(durationMillis = TransitionDuration)
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
        component(
            content = animatingContent,
            bendLevel = bendLevel,
            liquidLevel = liquidLevel,
        )
    }
}
