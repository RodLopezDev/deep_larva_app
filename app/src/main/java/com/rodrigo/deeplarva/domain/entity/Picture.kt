package com.rodrigo.deeplarva.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pictures",
    foreignKeys = [
        ForeignKey(
            entity = SubSample::class,
            parentColumns = ["id"],
            childColumns = ["sub_sample_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Picture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sub_sample_id")
    val subSampleId: Long = 0,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "thumb_path")
    val thumbFilePath: String,

    @ColumnInfo(name = "processed_path")
    val processedFilePath: String,

    @ColumnInfo(name = "has_metadata")
    val hasMetadata: Boolean,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "time")
    val time: Int
)