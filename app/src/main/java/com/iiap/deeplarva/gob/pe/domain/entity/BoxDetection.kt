package com.iiap.deeplarva.gob.pe.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "box_detection",
    foreignKeys = [
        ForeignKey(
            entity = Picture::class,
            parentColumns = ["id"],
            childColumns = ["picture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["picture_id"])]
)
data class BoxDetection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "picture_id")
    val pictureId: Long = 0,

    @ColumnInfo(name = "v1")
    val v1: Int,

    @ColumnInfo(name = "v2")
    val v2: Int,

    @ColumnInfo(name = "v3")
    val v3: Int,

    @ColumnInfo(name = "v4")
    val v4: Int,
)