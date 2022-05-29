package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.firethings.liquidmingler.R
import com.firethings.liquidmingler.state.BucketUpdate
import com.firethings.liquidmingler.state.BucketUpdateType

sealed class BucketVisuals {
    abstract val update: BucketUpdate
    abstract val size: DpSize
    abstract val bendCenterOffsetPercent: Float

    abstract val backRes: Int
    abstract val liquidRes: Int
    abstract val frontRes: Int

    val bucketId: Int get() = update.bucketId
    val updateType: BucketUpdateType get() = update.updateType
    val isSelected: Boolean get() = update.isSelected

    data class Flask(
        override val update: BucketUpdate
    ) : BucketVisuals() {
        override val size: DpSize = DpSize(54.dp, 62.dp)
        override val bendCenterOffsetPercent: Float = 0.1f

        override val backRes: Int = R.drawable.img_bottle_back
        override val liquidRes: Int = R.drawable.img_liquid
        override val frontRes: Int = R.drawable.img_bottle_front
    }
}
