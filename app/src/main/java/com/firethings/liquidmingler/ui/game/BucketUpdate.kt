package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import com.firethings.liquidmingler.state.BucketUpdateType

sealed class BucketUpdateLayoutData {
    abstract val bendMultiplier: Float
    abstract val isSelected: Boolean
    val scale: Float get() = if (isSelected) 1.3f else 1f

    data class None(override val isSelected: Boolean = false) : BucketUpdateLayoutData() {
        override val bendMultiplier = 1f
    }

    data class Fill(override val isSelected: Boolean = false) : BucketUpdateLayoutData() {
        override val bendMultiplier = 1f
    }

    data class Pour(
        override val bendMultiplier: Float,
        override val isSelected: Boolean,
        val translationX: Float,
        val translationY: Float
    ) : BucketUpdateLayoutData()
}

data class BucketVisualsWithLayout<out V : BucketVisuals>(
    val visuals: V,
    val layoutData: BucketUpdateLayoutData
) {
    val bucketId get() = visuals.update.bucketId
    val current get() = visuals.update.current
    val previous get() = visuals.update.previous
    val updateType get() = visuals.update.updateType
    val bendMultiplier get() = layoutData.bendMultiplier
    val isSelected get() = visuals.update.isSelected

    companion object {
        fun <V : BucketVisuals> wrap(
            visuals: V,
            layoutMap: Map<Int, LayoutCoordinates>,
            screenWidthPx: Float,
            pourWidthPx: Float
        ): BucketVisualsWithLayout<V> {
            return when (val updateType = visuals.updateType) {
                BucketUpdateType.Fill ->
                    BucketVisualsWithLayout(visuals, BucketUpdateLayoutData.Fill(isSelected = visuals.isSelected))
                BucketUpdateType.None ->
                    BucketVisualsWithLayout(visuals, BucketUpdateLayoutData.None(isSelected = visuals.isSelected))
                is BucketUpdateType.Pour -> {
                    val fromPos = layoutMap.getValue(visuals.bucketId)
                    val toPos = layoutMap.getValue(updateType.destinationId)
                    val fromBounds = fromPos.boundsInRoot()
                    val toBounds = toPos.boundsInRoot()

                    val bendMultiplier = when {
                        fromBounds.left < toBounds.left -> 1f
                        fromBounds.left > toBounds.left -> -1f
                        fromBounds.left < screenWidthPx -> -1f
                        else -> 1f
                    }

                    val translationX = toBounds.left - fromBounds.left - bendMultiplier * (
                            fromBounds.size.width * visuals.bendCenterOffsetPercent + pourWidthPx / 2f)
                    val translationY = toBounds.top - fromBounds.top

                    BucketVisualsWithLayout(
                        visuals, BucketUpdateLayoutData.Pour(
                            bendMultiplier = bendMultiplier,
                            isSelected = visuals.isSelected,
                            translationX = translationX,
                            translationY = translationY
                        )
                    )
                }
            }
        }
    }
}
