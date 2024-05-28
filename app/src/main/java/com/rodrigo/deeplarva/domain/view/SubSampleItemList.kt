package com.rodrigo.deeplarva.domain.view

data class SubSampleItemList (
    val id: Long = 0,
    val isTraining: Boolean,
    val mean: Int,
    val min: Int,
    val max: Int,
    val counts: Int
)