package com.firethings.liquidmingler.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalDensity
import com.firethings.liquidmingler.state.BucketUpdateType

sealed class BucketUpdateLayoutData {
    abstract val scale: Float

    data class None(override val scale: Float) : BucketUpdateLayoutData() {
        constructor(isSelected: Boolean) : this(getScale(isSelected))
    }

    data class Fill(override val scale: Float) : BucketUpdateLayoutData() {
        constructor(isSelected: Boolean) : this(getScale(isSelected))
    }

    data class Pour(
        override val scale: Float,
        val pourDirection: PourDirection,
        val translationX: Float,
        val translationY: Float,
        val pourTranslationX: Float,
        val pourTranslationY: Float
    ) : BucketUpdateLayoutData() {
        val pourMultiplier: Float get() = pourDirection.multiplier

        constructor(
            isSelected: Boolean,
            pourDirection: PourDirection,
            translationX: Float,
            translationY: Float,
            pourTranslationX: Float,
            pourTranslationY: Float
        ) : this(
            getScale(isSelected),
            pourDirection,
            translationX,
            translationY,
            pourTranslationX,
            pourTranslationY
        )
    }

    companion object {
        fun getScale(isSelected: Boolean) = if (isSelected) 1.3f else 1f
    }
}

enum class PourDirection(val multiplier: Float) {
    LEFT(1f), RIGHT(-1f)
}

data class BucketVisualsWithLayout<out V : BucketVisuals>(
    val visuals: V,
    val layoutData: BucketUpdateLayoutData
) {
    val current get() = visuals.update.current
    val previous get() = visuals.update.previous

    companion object {
        fun <V : BucketVisuals> wrap(
            visuals: V,
            layoutMap: Map<Int, LayoutCoordinates>,
            screenWidthPx: Float,
            pourWidthPx: Float,
            pourOffsetPx: Float
        ): BucketVisualsWithLayout<V> {
            return when (val updateType = visuals.updateType) {
                BucketUpdateType.Fill ->
                    BucketVisualsWithLayout(visuals, BucketUpdateLayoutData.Fill(visuals.isSelected))
                BucketUpdateType.None ->
                    BucketVisualsWithLayout(visuals, BucketUpdateLayoutData.None(visuals.isSelected))
                is BucketUpdateType.Pour -> {
                    val fromPos = layoutMap.getValue(visuals.bucketId)
                    val toPos = layoutMap.getValue(updateType.destinationId)
                    val fromBounds = fromPos.boundsInRoot()
                    val toBounds = toPos.boundsInRoot()

                    val pourDirection = when {
                        fromBounds.left < toBounds.left -> PourDirection.LEFT
                        fromBounds.left > toBounds.left -> PourDirection.RIGHT
                        fromBounds.left < screenWidthPx -> PourDirection.RIGHT
                        else -> PourDirection.LEFT
                    }

                    val translationX = toBounds.left - fromBounds.left - pourDirection.multiplier * (
                            fromBounds.size.width * visuals.bendCenterOffsetPercent + pourWidthPx / 2f)
                    val translationY = toBounds.top - fromBounds.top - pourOffsetPx

                    val pourTranslationX = toBounds.left - fromBounds.left + toBounds.size.width/2f - pourWidthPx/2f
                    val pourTranslationY = toBounds.top - fromBounds.top - pourOffsetPx

                    BucketVisualsWithLayout(
                        visuals, BucketUpdateLayoutData.Pour(
                            isSelected = visuals.isSelected,
                            pourDirection = pourDirection,
                            translationX = translationX,
                            translationY = translationY,
                            pourTranslationX = pourTranslationX,
                            pourTranslationY = pourTranslationY
                        )
                    )
                }
            }
        }
    }
}
