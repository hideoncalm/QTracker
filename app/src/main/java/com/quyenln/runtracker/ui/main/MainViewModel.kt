package com.quyenln.runtracker.ui.main

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyenln.runtracker.data.model.Run
import com.quyenln.runtracker.data.repository.MainRepository
import com.quyenln.runtracker.utils.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val runSortByDate = repository.getAllRunsSortByDate()
    private val runSortByCalories = repository.getAllRunsSortedByCalories()
    private val runSortByDistances = repository.getAllRunsSortedByDistance()
    private val runSortBySpeeds = repository.getAllRunsSortedBySpeed()
    private val runSortByTimeMillis = repository.getAllRunsSortedByTimeMillis()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE

    init {
        runs.addSource(runSortByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortByCalories) { result ->
            if (sortType == SortType.CALORIES) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortByDistances) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortBySpeeds) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortByTimeMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runSortByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortByTimeMillis.value?.let { runs.value = it }
        SortType.CALORIES -> runSortByCalories.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortBySpeeds.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortByDistances.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        repository.insertRun(run)
    }
}