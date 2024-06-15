package com.rodrigo.deeplarva.infraestructure.internal.driver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rodrigo.deeplarva.domain.entity.BoxDetection
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.internal.repository.BoxDetectionDAO
import com.rodrigo.deeplarva.infraestructure.internal.repository.PictureDAO

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