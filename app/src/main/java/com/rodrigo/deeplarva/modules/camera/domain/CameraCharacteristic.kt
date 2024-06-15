package com.rodrigo.deeplarva.modules.camera.domain

import android.util.Range
import android.util.Size

data class CameraCharacteristic (
    val largest: Size,
    private val isoRange: Range<Int>?,
    private val speedRange: Range<Long>?
) {
    fun getIsoRangeUpper(): Int {
        return isoRange?.upper ?: 0
    }
    fun getIsoRangeLower(): Int {
        return isoRange?.lower ?: 0
    }
    fun getSpeedRangeUpper(): Long {
        return speedRange?.upper ?: 0
    }
    fun getSpeedRangeLower(): Long {
        return speedRange?.lower ?: 0
    }
}