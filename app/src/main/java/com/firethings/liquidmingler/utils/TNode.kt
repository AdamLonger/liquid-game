package com.firethings.liquidmingler.utils

import android.util.Log
import androidx.compose.ui.graphics.toArgb
import java.lang.Math.ceil

class TNode(v: Array<TVial>) {
    var vial: Array<TVial> = v
    var mvInfo: TMoveInfo = TMoveInfo()

    constructor(def: TVialsDef) : this(
        Array(def.size) { index ->
            TVial(def[index], index)
        }
    )

    fun copy(): TNode = TNode(vial.map { TVial(it.color, it.pos) }.toTypedArray())

    fun print() {
        Log.v("LONGLOG", "\n _______________________________\n" +
                vial.joinToString(separator = "¨\n") {
                    it.pos.toString() + ": " +
                            it.color.joinToString(", ") { color -> color.name }
                } + "\n _______________________________")
    }

    fun nodeBlocks(): Int {
        var result = 0
        for (i in 0 until NVIALS) {
            result += vial[i].vialBlocks()
        }
        return result
    }

    //test vials for equality. vials are assumed to be already sorted.
    fun equalQ(node: TNode): Boolean {
        for (i in 0 until NVIALS) {
            for (j in 0 until NVOLUME) {
                if (vial[i].color[j] != node.vial[i].color[j]) return false
            }
        }
        return true
    }

    fun emptyVials(): Int = vial.map { it.isEmpty() }.count { it }

    fun lastMoves(): String {
        //TODO
        val ft = "%d->%d"
        var result = ""

        // EYSZERŰEN NEM ÉRTEM

//        for(i in 1..NCOLORS){
//            var j = NVIALS-1
//            while (vial[j].getTopInfo().topColor != i)
//        }
        return "Hellno"
    }

    fun nLastMoves(): Int {
        var result = NEMPTYVIALS

        for (i in 0 until NEMPTYVIALS) {
            if (vial[i].isEmpty()) result--
        }
        return result
    }

    fun sortNode(iLow: Int, iHigh: Int) {
        var low = iLow
        var high = iHigh
        val pivot = vial[(low + high) / 2]

        do {
            while (vial[low].compare(pivot) == 1) {
                low++
            }
            while (vial[high].compare(pivot) == -1) {
                high--
            }

            if (low <= high) {
                val t = vial[low]
                vial[low] = vial[high]
                vial[high] = t
                low++
                high--
            }
        } while (low <= high)

        if (high > iLow) sortNode(iLow, high)
        if (low < iHigh) sortNode(low, iHigh)
    }
}