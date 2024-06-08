package com.rodrigo.deeplarva.infraestructure.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.domain.entity.Picture

@Dao
interface BoxDetectionDAO {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    fun insert(box: BoxDetection)

    @Query("SELECT * FROM box_detection WHERE picture_id = :pictureId")
    fun getByPictureId(pictureId: Long): List<BoxDetection>
}