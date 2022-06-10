package com.firethings.liquidmingler.utils

import android.util.Log
import androidx.compose.ui.graphics.Color
import java.lang.Integer.min

typealias TVialsDef = Array<Array<TCls>>
typealias TState = Array<Array<MutableList<TNode>>>

const val NCOLORS = 3
const val NEMPTYVIALS = 2
const val NVOLUME = 4
const val NVIALS = 5
const val N_NOTDECREASE = 1000
const val N_MAXNODES = 2000000 //Abort after this many visits

enum class TCls {
    EMPTY,
    BLUE,
    RED,
    LIME,
    YELLOW,
    FUCHSIA,
    AQUA,
    GRAY,
    ROSE,
    OLIVE,
    BROWN,
    LBROWN,
    GREEN,
    LBLUE,
    BLACK
}

var state: TState = Array(NCOLORS * (NVOLUME - 1) + 1) { dimen1 ->
    Array(N_NOTDECREASE + 1) { dimen2 ->
        mutableListOf()
    }
}

fun solveMulti(def: TVialsDef) {
    var nd = TNode(def)
    nd.sortNode(0, NVIALS - 1)
    var y = 0
    val nBlockV = nd.nodeBlocks()

    for (i in 0..(nBlockV - NCOLORS)) {
        state[i][y].clear()
    }
    state[0][0].add(nd)

    var total = 1
    var solutionFound = false
    var newNodes = 0

    do {
        newNodes = 0
        for (i in 0..(nBlockV - NCOLORS)) {
            state[i][y + 1].clear()//prepare next column
        }

        for (x in 0 until nBlockV - NCOLORS) {
            val ndlist = state[x][y]

            for (i in 0 until ndlist.size) {
                nd = ndlist[i].copy()
                for (ks in 0 until NVIALS) {

                    val viS = nd.vial[ks].getTopInfo()
                    if (viS.emptyVolume == NVOLUME) continue//source is empty vial
                    for (kd in 0 until NVIALS) {
                        if (kd == ks) continue//source vial= destination vial
                        val viD = nd.vial[kd].getTopInfo()

                        if (viD.emptyVolume == 0 || //destination vial full
                            ((viD.emptyVolume < NVOLUME) && (viS.topVolume != viD.topVolume)) || //destination not empty and top colors different
                            ((viD.emptyVolume == NVOLUME) && (viS.topVolume + viS.emptyVolume == NVOLUME))
                        ) { // destinaion empty and only one color in source
                            continue
                        }

                        //two color blocks are merged
                        val blockdecreaseQ = viD.emptyVolume < NVOLUME && viD.emptyVolume >= viS.topVolume

                        val vmin = min(viD.emptyVolume, viS.topVolume)
                        val ndnew = nd.copy()

                        for (j in 1..vmin) {
                            ndnew.vial[kd].color[viD.emptyVolume - j] = viS.topColor
                            ndnew.vial[ks].color[viS.emptyVolume - 1 + j] = TCls.EMPTY
                        }

                        ndnew.sortNode(0, NVIALS - 1)
                        total++

                        if (total > N_MAXNODES) {
                            throw Exception("Node limit exceeded")
                        }

                        ndnew.mvInfo = TMoveInfo(
                            srcVial = nd.vial[ks].pos,
                            dstVial = nd.vial[kd].pos,
                            merged = blockdecreaseQ
                        )

                        if (blockdecreaseQ) {
                            state[x + 1][y].add(ndnew)
                        } else {
                            state[x][y + 1].add(ndnew)
                            newNodes++
                        }
                    }
                }
            }
        }
        solutionFound = state[nBlockV - NCOLORS][y].size > 0
        y++

    } while (!solutionFound && newNodes != 0)

    Log.v("LONGLOG", "$total nodes generated!")
    Log.v("LONGLOG", optimalSolution_multi(nBlockV, y - 1))
}

fun optimalSolution_multi(nblock: Int, y0: Int): String {
    if (state[nblock - NCOLORS][y0].size == 0) return "No solution. Undo moves or create new puzzle."
    if (nblock == NCOLORS) return "Puzzle already solved!"
    val ft = if (NVIALS > 9) "%2d->%2d," else "%d->%d,"

    var x = nblock - NCOLORS
    var y = y0

    var nd = state[x][y][0].copy()
    var src = nd.mvInfo.srcVial
    var dst = nd.mvInfo.dstVial

    var result = String.format(ft, src + 1, dst + 1)

    if (nd.mvInfo.merged) x-- else y--

    var solLength = 1
    while (x != 0 || y != 0) {
        val ndlist = state[x][y]

        for (i in 0 until ndlist.size) {
            val ndcand = ndlist[i].copy()

            var ks = 0
            while (ndcand.vial[ks].pos != src) ks++
            var kd = 0
            while (ndcand.vial[kd].pos != dst) kd++

            val viS = ndcand.vial[ks].getTopInfo()
            val viD = ndcand.vial[kd].getTopInfo()
            if (viS.emptyVolume == NVOLUME) {
                //ndcand.Free
                continue;//source is empty vial
            }

            if ((viD.emptyVolume == 0)/*destination vial full*/ ||
                (viD.emptyVolume < NVOLUME && viS.topColor != viD.topColor) ||
                /*destination not empty and top colors different*/
                (viD.emptyVolume == NVOLUME && viS.topVolume + viS.emptyVolume == NVOLUME)
            /*destination empty and only one color in source*/) {
                //ndcand.Free
                continue
            }

            val vmin = min(viD.emptyVolume, viS.topVolume)
            for (j in 1..vmin) {
                ndcand.vial[kd].color[viD.emptyVolume - j] = viS.topColor
                ndcand.vial[ks].color[viS.emptyVolume - 1 + j] = TCls.EMPTY
            }

            ndcand.sortNode(0, NVIALS - 1)

            if (nd.equalQ(ndcand)) {
                 nd = ndlist[i].copy()
                 src = nd.mvInfo.srcVial
                 dst = nd.mvInfo.dstVial
                result += String.format(ft, src + 1, dst + 1)
                solLength++
                if (nd.mvInfo.merged) x-- else y++
            }
        }
    }

    return "Optimal solution in $solLength moves"
}
