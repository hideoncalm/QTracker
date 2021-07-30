package com.quyenln.runtracker.ui.statistics

import androidx.lifecycle.ViewModel
import com.quyenln.runtracker.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalDistance = repository.getTotalDistances()
    val totalCalories = repository.getTotalCaloriesBurned()
    val avgSpeed = repository.getTotalAvgSpeed()
    val runSortByDate = repository.getAllRunsSortByDate()
}