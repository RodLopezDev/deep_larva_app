package com.odrigo.recognitionappkt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.odrigo.recognitionappkt.domain.Picture
import com.odrigo.recognitionappkt.domain.SubSample

@Database(entities = [SubSample::class, Picture::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subSample(): SubSampleDao
    abstract fun picture(): PictureDao
}