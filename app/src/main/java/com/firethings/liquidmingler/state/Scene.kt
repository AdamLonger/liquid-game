package com.firethings.liquidmingler.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
