package com.quyenln.runtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.quyenln.runtracker.data.db.utils.BitmapConverter
import com.quyenln.runtracker.data.model.Run

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(BitmapConverter::class)
abstract class RunDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDao
}