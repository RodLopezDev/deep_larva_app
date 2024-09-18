package com.deeplarva.iiap.gob.pe.utils

import com.deeplarva.iiap.gob.pe.application.adapters.CameraParameterAdapter
import kotlin.math.floor

class ExposureUtils {
    companion object {
        fun expoStepToValidStep(value: Float): Float {
            val isHonor = android.os.Build.BRAND == "HONOR"
            if(isHonor){
                return value * 100;
            }
            return value * 10;
        }
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
        fun convertLocalToLabel(value: Int, fixedStep: Float): String {
//            return value * 5F / 10F // -2.0, 2.0
            return "${floor(value * fixedStep).toInt()} / 10"
        }
        // -4 to 4
        fun convertLocalToCameraValue(value: Int, maxExposure: Int): Int {
            return value * (maxExposure / CameraParameterAdapter.EXPOSURE_FACTOR) // -2.0, 2.0
        }
    }
}