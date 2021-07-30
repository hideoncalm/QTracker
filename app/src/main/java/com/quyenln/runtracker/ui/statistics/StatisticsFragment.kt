package com.quyenln.runtracker.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.quyenln.runtracker.R
import com.quyenln.runtracker.base.BaseFragment
import com.quyenln.runtracker.databinding.FragmentStatisticsBinding
import com.quyenln.runtracker.utils.getFormattedStopWatchTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*

@AndroidEntryPoint
class StatisticsFragment : BaseFragment<FragmentStatisticsBinding>() {

    private val viewModel: StatisticsViewModel by viewModels()

    override val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentStatisticsBinding =
        FragmentStatisticsBinding::inflate

    override fun initViews(savedInstanceState: Bundle?) {
        setUpBarChart()
        subscribeToObserver()
    }

    override fun initListeners() {
    }

    private fun subscribeToObserver() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                tvTotalDistance.text = "${km} km"
            }
        })
        viewModel.avgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                tvAverageSpeed.text = "${it} km/h"
            }
        })
        viewModel.totalCalories.observe(viewLifecycleOwner, Observer {
            it?.let {
                tvTotalCalories.text = "${it} kcal"
            }
        })
        viewModel.runSortByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeed!!) }
                val barDataset = BarDataSet(allAvgSpeeds, "AVG Speed").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                barChart.apply {
                    data = BarData(barDataset)
                    invalidate()
                    marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.custom_marker_view)
                }
            }
        })
    }

    private fun setUpBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTH_SIDED
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "AVG Speed"
            legend.isEnabled = false
        }
    }
}