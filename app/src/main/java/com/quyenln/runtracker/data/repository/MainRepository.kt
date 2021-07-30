package com.quyenln.runtracker.data.repository

import com.quyenln.runtracker.data.db.RunDao
import com.quyenln.runtracker.data.model.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeMillis() = runDao.getAllRunsSortedByTimeMillis()

    fun getAllRunsSortedBySpeed() = runDao.getAllRunsSortedBySpeed()

    fun getAllRunsSortedByCalories() = runDao.getAllRunsSortedByCalories()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistances() = runDao.getTotalDistances()

}