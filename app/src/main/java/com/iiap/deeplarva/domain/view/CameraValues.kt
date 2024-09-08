package com.iiap.deeplarva.domain.view

data class CameraValues (
    val maxWidth: Int,
    val maxHeight: Int,
    var sensorSensitivity: Int,
    val sensorSensitivityMin: Int,
    val sensorSensitivityMax: Int,
    var exposure: Int,
    val exposureMin: Int,
    val exposureMax: Int,
    var shootSpeed: Long,
    var shootSpeedText: String,
)