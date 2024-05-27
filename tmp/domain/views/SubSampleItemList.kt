package com.odrigo.recognitionappkt.domain.views

data class SubSampleItemList(
    val id: Long = 0,
    val isTraining: Boolean,
    val mean: Int,
    val min: Int,
    val max: Int,
    val counts: Int,
)