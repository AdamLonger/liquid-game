package com.firethings.liquidmingler.state

sealed class Liquid {
    abstract val id: Int

    object Red : Liquid() {
        override val id: Int = 0
    }

    object Blue : Liquid() {
        override val id: Int = 1
    }

    object Green : Liquid() {
        override val id: Int = 3
    }
}
