package com.odrigo.recognitionappkt.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.odrigo.recognitionappkt.domain.Picture

@Dao
interface PictureDao {
    @Query("SELECT * FROM pictures where id = :id")
    fun getById(id: Long): List<Picture>

    @Query("SELECT * FROM pictures")
    fun getAllPictures(): List<Picture>

    @Query("SELECT * FROM pictures WHERE sub_sample_id = :subSampleId")
    fun getBySubSampleId(subSampleId: Long): List<Picture>

    @Query("SELECT * FROM pictures WHERE has_metadata = 1 and sub_sample_id = :subSampleId")
    fun getBySubSampleIdProcessed(subSampleId: Long): List<Picture>

    @Query("SELECT * FROM pictures WHERE has_metadata = 0 and sub_sample_id = :subSampleId")
    fun getBySubSampleIdNonProcessed(subSampleId: Long): List<Picture>

    @Insert
    fun insert(user: Picture)

    @Update
    fun update(user: Picture)

    @Delete
    fun delete(user: Picture)
}