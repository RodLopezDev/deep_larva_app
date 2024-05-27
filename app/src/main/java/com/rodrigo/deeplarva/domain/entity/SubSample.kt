package com.rodrigo.deeplarva.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sub_sample")
data class SubSample (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "is_training")
    val isTraining: Boolean,

    @ColumnInfo(name = "mean")
    val mean: Float,

    @ColumnInfo(name = "average")
    val average: Float,

    @ColumnInfo(name = "min")
    val min: Float,

    @ColumnInfo(name = "max")
    val max: Float
)