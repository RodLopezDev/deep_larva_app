package com.rodrigo.deeplarva.utils

import android.graphics.Color

class Colors {
    companion object {
        fun green(opacity: Int = 100): Int {
            return  Color.parseColor("#${opacity}124116")
        }
        fun red(opacity: Int = 100): Int {
            return  Color.parseColor("#${opacity}5f0937")
        }
    }
}