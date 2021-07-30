package com.quyenln.runtracker.data.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run")
data class Run(
    var image: Bitmap? = null,
    var timestamp: Long? = 0L,  // time start run
    var avgSpeed: Float? = 0f,
    var distanceMeters: Int = 0,
    var timeInMillis: Long = 0L,  // time for how long to run
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}