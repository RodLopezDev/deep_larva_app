package com.rodrigo.deeplarva.infraestructure

import androidx.room.Room
import android.content.Context
import androidx.annotation.NonNull
import com.rodrigo.deeplarva.domain.Constants

import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase

class DbBuilder {
    companion object {
        fun getInstance(@NonNull ctx: Context): AppDatabase {
            return Room.databaseBuilder(ctx, AppDatabase::class.java, Constants.DB_NAME).build()
        }
    }
}