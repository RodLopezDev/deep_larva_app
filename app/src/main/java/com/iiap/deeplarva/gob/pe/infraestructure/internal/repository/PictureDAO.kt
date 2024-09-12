package com.iiap.deeplarva.gob.pe.infraestructure.internal.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import com.iiap.deeplarva.gob.pe.domain.entity.Picture


@Dao
interface PictureDAO {
    @Query("SELECT * FROM picture where id = :id ")
    fun getById(id: Long): List<Picture>

    @Query("SELECT * FROM picture ORDER BY timestamp DESC")
    fun getAllPictures(): List<Picture>

    @Query("SELECT * FROM picture WHERE has_metadata = 1 and sync = 0")
    fun getAllProcessedNonSync(): List<Picture>

    @Query("SELECT * FROM picture WHERE has_metadata = 0")
    fun getAllNonProcessed(): List<Picture>

    @Insert
    fun insert(user: Picture)

    @Update
    fun update(user: Picture)

    @Delete
    fun delete(user: Picture)
}