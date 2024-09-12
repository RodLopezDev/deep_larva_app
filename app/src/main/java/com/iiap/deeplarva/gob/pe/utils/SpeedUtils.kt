package com.iiap.deeplarva.utils

class SpeedUtils {
    companion object {
        fun shutterMlToString(value: Int): String {
            if(value == 17) {
                return "1 / 60"
            }
            if(value == 20) {
                return "1 / 50"
            }
            if(value == 13) {
                return "1 / 80"
            }
            if(value == 10) {
                return "1 / 100"
            }
            if(value == 1) {
                return "1 / 1000"
            }
            return "$value ms"
        }
    }
}