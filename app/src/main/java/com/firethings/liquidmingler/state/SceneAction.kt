package com.firethings.liquidmingler.state

sealed class SceneAction {
    data class BucketTap(val bucketId: Int) : SceneAction()
    object Reset : SceneAction()
}
