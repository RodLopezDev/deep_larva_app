package com.rodrigo.deeplarva.infraestructure.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.rodrigo.deeplarva.domain.entity.BoxDetection

@Dao
interface BoxDetectionDAO {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    fun insert(box: BoxDetection)
}