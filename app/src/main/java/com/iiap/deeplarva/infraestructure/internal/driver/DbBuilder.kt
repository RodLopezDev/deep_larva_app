package com.rodrigo.deeplarva.infraestructure.internal.driver

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Room
import com.rodrigo.deeplarva.application.utils.Constants

class DbBuilder {
    companion object {
        fun getInstance(@NonNull ctx: Context): AppDatabase {
            return Room.databaseBuilder(ctx, AppDatabase::class.java, Constants.DB_NAME).build()
        }
    }
}