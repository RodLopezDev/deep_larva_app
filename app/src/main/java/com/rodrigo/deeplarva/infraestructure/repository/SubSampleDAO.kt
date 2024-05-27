package com.rodrigo.deeplarva.infraestructure.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rodrigo.deeplarva.domain.entity.SubSample

@Dao
interface SubSampleDAO {
    @Query("SELECT * FROM sub_sample WHERE id = :id")
    fun getById(id: Long): List<SubSample>

    @Query("SELECT * FROM sub_sample")
    fun getAllSubSamples(): List<SubSample>

    //@Query("SELECT ss.id, ss.is_training as isTraining, ss.mean, ss.min, ss.max, (SELECT count(p.id) FROM pictures as p WHERE p.sub_sample_id = ss.id) as counts FROM sub_sample as ss")
    //fun getAllSubSamplesForUIList(): List<SubSampleItemList>

    @Insert
    fun insert(user: SubSample)

    @Update
    fun update(user: SubSample)

    @Delete
    fun delete(user: SubSample)
}