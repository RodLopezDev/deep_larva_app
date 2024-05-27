package com.rodrigo.deeplarva.infraestructure

import androidx.room.Room
import android.content.Context
import androidx.annotation.NonNull

import com.rodrigo.deeplarva.infraestructure.driver.AppDatabase

class Builder {
    companion object {
        fun getInstance(@NonNull ctx: Context): AppDatabase {
            val dbName = "deep-larva-db"
            return Room.databaseBuilder(ctx, AppDatabase::class.java, dbName).build()
        }
    }
}