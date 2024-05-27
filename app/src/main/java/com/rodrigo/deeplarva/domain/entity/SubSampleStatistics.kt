package com.rodrigo.deeplarva.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_sample_statistic",
    foreignKeys = [
        ForeignKey(
            entity = SubSample::class,
            parentColumns = ["id"],
            childColumns = ["sub_sample_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubSampleStatistics (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sub_sample_id")
    val subSampleId: Long = 0,

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