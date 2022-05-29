package com.firethings.liquidmingler.ui.game

const val MAX_Z_INDEX = 1000f
enum class BucketLayer(val zIndexMultiplier:Float) {
    BACKGROUND(0f),
    STREAM(200f),
    LIQUID(300f),
    FOREGROUND(400f),
}