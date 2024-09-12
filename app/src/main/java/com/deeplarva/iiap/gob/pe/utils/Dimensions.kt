package com.deeplarva.iiap.gob.pe.utils

import android.content.Context
import android.util.TypedValue

class Dimensions(private val context: Context) {
    fun dpToPx( dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
}