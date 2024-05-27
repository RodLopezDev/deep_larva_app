package com.odrigo.recognitionappkt.db

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Room

class BDFactory {
    companion object {
        fun getInstance(@NonNull ctx: Context): AppDatabase {
            return Room.databaseBuilder(
                ctx,
                AppDatabase::class.java, "larvas-db"
            ).build()
        }
    }
}