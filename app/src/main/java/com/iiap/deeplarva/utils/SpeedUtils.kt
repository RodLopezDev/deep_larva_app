package com.iiap.deeplarva.utils

import kotlin.math.log10
import kotlin.math.pow

class SpeedUtils {
    companion object {
        fun adjustSpeed(value: Int): Int {
            return ((value + 5) / 10) * 10
        }
        fun speedToText(value: Int): String {
            if(value == 0) {
                return "Auto"
            }

            val fraction = value.toDouble() / 1000

            val denominator = 10.0.pow(-log10(fraction)).toInt()
            val adjustedDenominator = adjustSpeed(denominator)

            val numerator = (fraction * adjustedDenominator).toInt()
            return if (numerator == 1) "1 / $adjustedDenominator" else "$numerator / $adjustedDenominator"
        }
    }
}