package com.iiap.deeplarva.gob.pe.infraestructure.internal.driver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iiap.deeplarva.gob.pe.domain.entity.BoxDetection
import com.iiap.deeplarva.gob.pe.domain.entity.Picture
import com.iiap.deeplarva.gob.pe.infraestructure.internal.repository.BoxDetectionDAO
import com.iiap.deeplarva.gob.pe.infraestructure.internal.repository.PictureDAO

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