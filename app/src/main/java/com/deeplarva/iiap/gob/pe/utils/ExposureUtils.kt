package com.deeplarva.iiap.gob.pe.utils

import com.deeplarva.iiap.gob.pe.application.adapters.CameraParameterAdapter

class ExposureUtils {
    companion object {
        // -2 to 2
        fun convertServerToValue(value: Float): Int {
            if(value > CameraParameterAdapter.EXPOSURE_MAX ||
                value < CameraParameterAdapter.EXPOSURE_MIN
            ){
                return 0
            }
            val value2 = value * 2
            return value2.toInt()
        }
        // -4 to 4
        fun convertLocalToLabel(value: Int): Float {
            return value * 5F / 10F
        }
    }
}