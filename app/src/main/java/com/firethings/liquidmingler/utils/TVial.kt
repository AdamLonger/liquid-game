package com.firethings.liquidmingler.utils

import androidx.compose.ui.graphics.vector.EmptyPath

class TVial(
    c: Array<TCls>,
    p: Int
) {
    val color: Array<TCls> = Array(NVOLUME) { idx -> c[idx] }
    val pos = p

    fun getTopInfo(): TVialTopInfo {
        var result = TVialTopInfo(topColor = TCls.EMPTY, topVolume = 0, emptyVolume = NVOLUME)

        if (color[NVOLUME - 1] == TCls.EMPTY) return result

        var cl: TCls = TCls.EMPTY

        for (i in 0 until NVOLUME) {
            if (color[i] != TCls.EMPTY) {
                cl = color[i]
                result = result.copy(topColor = cl, emptyVolume = i)
                break
            }
        }
        var topVol = 1

        for (i in result.emptyVolume + 1 until NVOLUME) {
            if (cl == color[i]) topVol++ else break
        }

        return result
    }

    fun vialBlocks(): Int {
        // color.toList().windowed(2, 1, false){ window-> window.first() == window.last()}

        var count = 1
        for (i in 0 until (NVOLUME - 1)) {
            if (color[i] != color[i + 1]) count++
        }
        if (color[0] == TCls.EMPTY) count--

        return count
    }

    fun isEmpty(): Boolean = color.filterNotNull().isEmpty()

    fun contentEquals(other: TVial) = other.color.mapIndexed { index, c -> color[index] == c }.all { it }

    override fun equals(other: Any?): Boolean {
        return if (other is TVial) {
            other.pos == pos &&
                    other.color.mapIndexed { index, c -> color[index] == c }.all { it }
        } else super.equals(other)
    }

    fun compare(v2: TVial): Int {
        for (i in 0 until NVOLUME) {
            if (color[i] < v2.color[i]) return 1
            if (color[i] > v2.color[i]) return -1
        }
        return 0
    }
}
