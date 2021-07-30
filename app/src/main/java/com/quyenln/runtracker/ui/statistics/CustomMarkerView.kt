package com.quyenln.runtracker.ui.statistics

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.quyenln.runtracker.data.model.Run
import com.quyenln.runtracker.utils.getFormattedStopWatchTime
import kotlinx.android.synthetic.main.custom_marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    private val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 0.1f, height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) return
        val curRunId = e.x.toInt()
        val run = runs[curRunId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp!!
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calendar.time)
        tvAvgSpeed.text = "${run.avgSpeed} km/h"
        tvDistance.text = "${run.distanceMeters / 1000f} km"
        tvTime.text = getFormattedStopWatchTime(run.timeInMillis)
        tvCalories.text = "${run.caloriesBurned} kcal"
    }
}