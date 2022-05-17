package com.firethings.liquidmingler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class Scene(val buckets: List<Bucket>) {
    private val mutableState: MutableStateFlow<GameState> = MutableStateFlow(GameState.initialFrom(buckets))
    val state: StateFlow<GameState> get() = mutableState

    private var stateJob: Job? = null
    val actionChannel: Channel<SceneAction> = Channel()

    fun startEventProcessing(scope: CoroutineScope) {
        if (stateJob == null) {
            stateJob = scope.launch {
                actionChannel.consumeAsFlow().collectLatest { action ->
                    val currentState = mutableState.value
                    val newState = when (action) {
                        is SceneAction.BucketTap -> when {
                            currentState.selected == null || currentState.selected.id == action.bucketId ->
                                GameLogic.toggleBucket(currentState, action.bucketId)
                            action.bucketId != currentState.selected.id ->
                                GameLogic.pourSelected(currentState, action.bucketId)
                            else -> currentState
                        }
                        SceneAction.Reset -> GameState.initialFrom(buckets)
                    }
                    mutableState.emit(newState)
                }
            }
        }
    }
}

sealed class SceneAction {
    data class BucketTap(val bucketId: Int) : SceneAction()
    object Reset : SceneAction()
}

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
        val selected = if (state.selected?.id == bucketId) {
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
        list.size + to.content.size > to.size -> false
        to.content.isNotEmpty() && list.first().id != to.content.first().id -> false
        else -> true
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

data class Bucket(
    val id: Int,
    val size: Int,
    var content: List<Liquid>
) {
    val isFull get() = content.size == size
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

sealed class Liquid {
    abstract val id: Int

    object Red : Liquid() {
        override val id: Int = 0
    }

    object Blue : Liquid() {
        override val id: Int = 1
    }
}
