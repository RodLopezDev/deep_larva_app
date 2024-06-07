package com.rodrigo.deeplarva.infraestructure.driver

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.infraestructure.repository.PictureDAO

@Database(
    entities = [
        Picture::class
   ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun picture(): PictureDAO
}