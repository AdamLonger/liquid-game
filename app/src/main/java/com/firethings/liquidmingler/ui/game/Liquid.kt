package com.firethings.liquidmingler.ui.game

import androidx.compose.ui.graphics.Color
import com.firethings.liquidmingler.state.Liquid
import kotlin.math.sqrt
import kotlin.math.tan

fun Liquid.color() = when (this) {
    Liquid.Blue -> Color.Blue
    Liquid.Red -> Color.Red
}

/**
 * Used to calculate the 2D shape of a liquid inside a rectangle tube.
 * It work by comparing the area of the shapes given that the area of the straight and the bent shapes must be equal.
 * The calculations are derived by are calculations and trigonometry
 * Area of a rectangle: width*height
 * Area of a triangle: width*height/2
 * Area of a trapeze: (topWidth + bottomWidth)/2 * height
 * Tangent calculation: Tan(alpha) = oppositeSideWidth / adjacentSideWidth
 */
fun calculateLiquidShape(
    width: Float,
    height: Float,
    angle: Float
): LiquidShape {
    val tanAlpha = tan(Math.toRadians(angle.toDouble())).toFloat()
    val trapezeBase = height - tanAlpha * width / 2f

    if (trapezeBase <= 0) {
        //The liquid can't reach both sides of the container at this angle, the shape is a triangle
        val adjacent = sqrt(2f * width * height / tanAlpha)
        val opposite = tanAlpha * adjacent

        return LiquidShape.Triangle(
            adjacentSize = adjacent,
            oppositeSize = opposite
        )
    } else {
        //The liquid reaches both side of the container at this angle, the shape is a trapeze
        val sideDifference = tanAlpha * width

        return LiquidShape.Trapeze(
            shorterSize = trapezeBase,
            longerSide = trapezeBase + sideDifference,
            height = height
        )
    }
}

sealed class LiquidShape {
    data class Triangle(
        val adjacentSize: Float,
        val oppositeSize: Float
    ) : LiquidShape()

    data class Trapeze(
        val shorterSize: Float,
        val longerSide: Float,
        val height: Float
    ) : LiquidShape()
}
