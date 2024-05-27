package com.odrigo.recognitionappkt.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sub_sample")
data class SubSample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "is_training")
    val isTraining: Boolean,

    @ColumnInfo(name = "mean")
    val mean: Int,

    @ColumnInfo(name = "min")
    val min: Int,

    @ColumnInfo(name = "max")
    val max: Int,
)