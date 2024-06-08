package com.rodrigo.deeplarva.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "picture",
)
data class Picture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "device_id")
    val deviceId: String,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String,

    @ColumnInfo(name = "processed_path")
    val processedFilePath: String,

    @ColumnInfo(name = "has_metadata")
    val hasMetadata: Boolean,

    @ColumnInfo(name = "count")
    val count: Int,

    @ColumnInfo(name = "time")
    val time: Long,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)