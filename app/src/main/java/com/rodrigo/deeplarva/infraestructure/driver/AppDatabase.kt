package com.rodrigo.deeplarva.infraestructure.driver

import androidx.room.Database
import androidx.room.RoomDatabase

import com.rodrigo.deeplarva.domain.entity.Picture
import com.rodrigo.deeplarva.domain.entity.SubSample

import com.rodrigo.deeplarva.infraestructure.repository.PictureDAO
import com.rodrigo.deeplarva.infraestructure.repository.SubSampleDAO

@Database(
    entities = [
        SubSample::class,
        Picture::class
   ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subSample(): SubSampleDAO
    abstract fun picture(): PictureDAO
}