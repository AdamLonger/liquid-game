package com.firethings.liquidmingler.state

data class Bucket(
    val id: Int,
    val size: Int,
    var content: List<Liquid>
) {
    val isFull get() = content.size == size
    val isEmpty get() = content.size == 0
    val isMonochrome get() = content.distinctBy { it.id }.size == 1
    val isComplete get() = (isMonochrome && isFull) || content.isEmpty()
    val availableSpace get() = size - content.size
    val topPortion
        get() = content.indexOfFirst { it.id != content.firstOrNull()?.id ?: -1 }.let { index ->
            return@let when {
                index >= 0 -> content.take(index)
                isMonochrome -> content
                else -> emptyList()
            }
        }
}

sealed class BucketUpdateType {
    object None : BucketUpdateType()
    object Fill : BucketUpdateType()
    data class Pour(val destinationId: Int) : BucketUpdateType()
}

data class BucketUpdate(
    val current: Bucket,
    val previous: Bucket,
    val isSelected: Boolean = false,
    val updateType: BucketUpdateType = BucketUpdateType.None
) {
    val bucketId = current.id
}