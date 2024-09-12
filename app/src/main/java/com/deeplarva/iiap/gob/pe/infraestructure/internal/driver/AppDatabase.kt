package com.deeplarva.iiap.gob.pe.infraestructure.internal.driver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deeplarva.iiap.gob.pe.domain.entity.BoxDetection
import com.deeplarva.iiap.gob.pe.domain.entity.Picture
import com.deeplarva.iiap.gob.pe.infraestructure.internal.repository.BoxDetectionDAO
import com.deeplarva.iiap.gob.pe.infraestructure.internal.repository.PictureDAO

@Database(
    entities = [
        Picture::class,
        BoxDetection::class
   ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun picture(): PictureDAO
    abstract fun boxDetection(): BoxDetectionDAO
}