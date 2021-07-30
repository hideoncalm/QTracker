package com.quyenln.runtracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.quyenln.runtracker.data.model.Run

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("select * from run order by timestamp desc")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("select * from run order by avgSpeed desc")
    fun getAllRunsSortedBySpeed(): LiveData<List<Run>>

    @Query("select * from run order by distanceMeters desc")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("select * from run order by timeInMillis desc")
    fun getAllRunsSortedByTimeMillis(): LiveData<List<Run>>

    @Query("select * from run order by caloriesBurned desc")
    fun getAllRunsSortedByCalories(): LiveData<List<Run>>

    @Query("select sum(timeInMillis) from run")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("select sum(caloriesBurned) from run")
    fun getTotalCaloriesBurned(): LiveData<Int>

    @Query("select sum(distanceMeters) from run")
    fun getTotalDistances(): LiveData<Int>

    @Query("select avg(avgSpeed) from run")
    fun getTotalAvgSpeed(): LiveData<Float>
}