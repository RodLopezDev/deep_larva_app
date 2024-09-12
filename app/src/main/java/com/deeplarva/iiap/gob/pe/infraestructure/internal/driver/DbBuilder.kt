package com.deeplarva.iiap.gob.pe.infraestructure.internal.driver

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Room
import com.deeplarva.iiap.gob.pe.domain.constants.AppConstants

class DbBuilder {
    companion object {
        fun getInstance(@NonNull ctx: Context): AppDatabase {
            return Room.databaseBuilder(ctx, AppDatabase::class.java, AppConstants.DB_NAME).build()
        }
    }
}