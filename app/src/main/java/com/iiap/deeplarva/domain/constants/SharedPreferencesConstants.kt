package com.iiap.deeplarva.domain.constants

class SharedPreferencesConstants {
    companion object {
        const val DEVICE_ID = "DEVICE-IDENTIFIER"

        const val RESOLUTION_MAX_WIDTH = "camera-resolution-max-width"
        const val RESOLUTION_MAX_HEIGHT = "camera-resolution-max-height"
        
        const val EXPOSURE_VALUE = "exposure-camera-value"
        const val EXPOSURE_MIN = "exposure-camera-min"
        const val EXPOSURE_MAX = "exposure-camera-max"

        const val SENSITIVITY_VALUE = "sensor-sensitivity-camera-value"
        const val SENSITIVITY_MIN = "sensor-sensitivity-camera-min"
        const val SENSITIVITY_MAX = "sensor-sensitivity-camera-max"

        const val EXPOSURE_TIME_VALUE = "sensor-exposure-time-camera-value"
        const val EXPOSURE_TIME_MIN = "sensor-exposure-time-camera-min"
        const val EXPOSURE_TIME_MAX = "sensor-exposure-time-camera-max"

        const val CONFIG_FLAG_INITIAL_CONFIG = "config_flag"
        const val CONFIG_CAMERA_ACTIVITY_V2 = "config_v2"
    }
}