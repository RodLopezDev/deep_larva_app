package com.odrigo.recognitionappkt.view

data class DeletableElement<T>(
    var hasFlag: Boolean,
    val item: T
)