package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.unit.Dp
import com.firethings.liquidmingler.state.BucketUpdate
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

data class BucketUpdateWithLayout(
    val update: BucketUpdate,
    val layoutData: BucketUpdateLayoutData
) {
    val bucketId get() = update.bucketId
    val current get() = update.current
    val previous get() = update.previous
    val updateType get() = update.updateType
    val bendMultiplier get() = layoutData.bendMultiplier
    val isSelected get() = update.isSelected

    companion object {
        fun wrap(
            update: BucketUpdate,
            layoutMap: Map<Int, LayoutCoordinates>,
            screenWidthPx: Float
        ): BucketUpdateWithLayout {
            return when (update.updateType) {
                BucketUpdateType.Fill ->
                    BucketUpdateWithLayout(update, BucketUpdateLayoutData.Fill(isSelected = update.isSelected))
                BucketUpdateType.None ->
                    BucketUpdateWithLayout(update, BucketUpdateLayoutData.None(isSelected = update.isSelected))
                is BucketUpdateType.Pour -> {
                    val fromPos = layoutMap.getValue(update.bucketId)
                    val toPos = layoutMap.getValue(update.updateType.destinationId)
                    val fromBounds = fromPos.boundsInRoot()
                    val toBounds = toPos.boundsInRoot()

                    val bendMultiplier = when {
                        fromBounds.left < toBounds.left -> 1f
                        fromBounds.left > toBounds.left -> -1f
                        fromBounds.left < screenWidthPx -> -1f
                        else -> 1f
                    }

                    val translationX = toBounds.left - fromBounds.left - bendMultiplier * fromPos.size.width
                    val translationY = toBounds.top - fromBounds.top

                    BucketUpdateWithLayout(
                        update, BucketUpdateLayoutData.Pour(
                            bendMultiplier = bendMultiplier,
                            isSelected = update.isSelected,
                            translationX = translationX,
                            translationY = translationY
                        )
                    )
                }
            }
        }
    }
}
