package com.iiap.deeplarva.domain.response

data class NewPictureResponse (
    val id: String,
    val originalFileURL: String,
    val processedFileURL: String,
    val circuitBreak: Boolean?,
)