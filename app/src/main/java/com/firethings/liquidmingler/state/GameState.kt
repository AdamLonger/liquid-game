package com.firethings.liquidmingler.state

data class GameState(
    val updates: List<BucketUpdate>,
    val selected: Bucket? = null
) {
    val buckets = updates.map { it.current }
    val previous = updates.map { it.previous }

    companion object {
        fun initialFrom(list: List<Bucket>) = GameState(
            selected = null,
            updates = list.map { BucketUpdate(current = it, previous = it) }
        )
    }
}

object GameLogic {
    fun toggleBucket(state: GameState, bucketId: Int): GameState {
        val bucket = state.buckets.firstOrNull { it.id == bucketId }
        val selected = if (state.selected?.id == bucketId || (state.selected == null && bucket?.isEmpty != false)) {
            null
        } else {
            state.buckets.first { it.id == bucketId }
        }

        return state.copy(
            selected = selected,
            updates = state.updates.map {
                it.copy(
                    isSelected = selected?.id == it.bucketId,
                    updateType = BucketUpdateType.None
                )
            }
        )
    }

    fun pourSelected(state: GameState, destinationBucketId: Int): GameState {
        val selectedBucket = state.selected ?: return state
        val portion = selectedBucket.topPortion

        if (portion.isEmpty()) {
            return state.copy(selected = null)
        }

        val destination = state.buckets.first { it.id == destinationBucketId }
        val newContent = if (canPut(destination, portion)) {
            val newFrom = take(from = selectedBucket, portion.size)
            val newTo = put(to = destination, portion)

            state.buckets.asSequence()
                .filterNot { it.id == newFrom.id || it.id == newTo.id }.map { BucketUpdate(it, it) }
                .plus(BucketUpdate(newFrom, selectedBucket, false, BucketUpdateType.Pour(newTo.id)))
                .plus(BucketUpdate(newTo, destination, false, BucketUpdateType.Fill))
                .sortedBy { it.bucketId }.toList()

        } else state.buckets
            .zip(state.buckets)
            .map { (current, previous) -> BucketUpdate(current, previous) }

        return state.copy(selected = null, updates = newContent)
    }

    private fun take(from: Bucket, amount: Int): Bucket = from.copy(content = from.content.drop(amount))
    private fun put(to: Bucket, list: List<Liquid>) = to.copy(content = list + to.content)
    private fun canPut(to: Bucket, list: List<Liquid>) = when {
        list.isEmpty() -> false
        list.distinctBy { it.id }.size != 1 -> false
        list.size + to.content.size > to.volume -> false
        to.content.isNotEmpty() && list.first().id != to.content.first().id -> false
        else -> true
    }
}
