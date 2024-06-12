package com.rodrigo.deeplarva.domain.response

data class NewPictureResponse (
    val id: String,
    val originalFileURL: String,
    val processedFileURL: String,
)