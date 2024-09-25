package com.deeplarva.iiap.gob.pe.utils

import com.deeplarva.iiap.gob.pe.application.adapters.CameraParameterAdapter
import com.deeplarva.iiap.gob.pe.domain.constants.CornerCasesConstants

class ExposureUtils {
    companion object {
        fun expoStepToValidStep(value: Float): Float {
            val cornerCase = CornerCasesConstants.BRAND_CORNER_CASES_EXPOSURE_STEP.indexOf(android.os.Build.BRAND) != -1
            if(cornerCase){
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
        // Input: -4 to 4
        fun convertLocalToLabel(value: Int, fixedStep: Float): String {
            // Show values between -2 and 2
            return (value / 2F).toString()
            //return "${floor(value * fixedStep).toInt()} / 10"
        }
    }
}