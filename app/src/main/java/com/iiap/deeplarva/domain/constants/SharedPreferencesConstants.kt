package com.iiap.deeplarva.domain.constants

class SharedPreferencesConstants {
    companion object {
        const val DEVICE_ID = "DEVICE-IDENTIFIER"

        const val SHARED_PREFERENCES_RESOLUTION_MAX_WIDTH = "camera-resolution-max-width"
        const val SHARED_PREFERENCES_RESOLUTION_MAX_HEIGHT = "camera-resolution-max-height"
        const val SHARED_PREFERENCES_EXPOSURE_VALUE = "exposure-camera-value"
        const val SHARED_PREFERENCES_EXPOSURE_MIN = "exposure-camera-min"
        const val SHARED_PREFERENCES_EXPOSURE_MAX = "exposure-camera-max"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_VALUE = "sensor-sensitivity-camera-value"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_MIN = "sensor-sensitivity-camera-min"
        const val SHARED_PREFERENCES_SENSOR_SENSITIVITY_MAX = "sensor-sensitivity-camera-max"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_VALUE = "sensor-exposure-time-camera-value"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MIN = "sensor-exposure-time-camera-min"
        const val SHARED_PREFERENCES_SENSOR_EXPOSURE_TIME_MAX = "sensor-exposure-time-camera-max"

        const val CONFIG_SHARED_PREFERENCES_FLAG_INITIAL_CONFIG = "config_flag"
        const val CONFIG_SHARED_PREFERENCES_CAMERA_ACTIVITY_V2 = "config_v2"

        const val CLOUD_VALUE_LAST_DATE_CHECKED = "cloud-last-date-checked"
        const val CLOUD_VALUE_APP_VERSION = "cloud-app-version"
        const val CLOUD_VALUE_SERVER_URL = "cloud-server-url"
        const val CLOUD_VALUE_SERVER_API_KEY = "cloud-server-api-key"
    }
}