package com.iiap.deeplarva.domain.view

data class CameraValues (
    val maxWidth: Int,
    val maxHeight: Int,
    var sensorSensitivity: Int,
    var exposureMin: Int,
    var exposureMax: Int,
    var exposure: Int,
    var shootSpeed: Int,
    var shootSpeedText: String,
)