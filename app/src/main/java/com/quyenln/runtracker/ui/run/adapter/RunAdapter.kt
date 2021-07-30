package com.quyenln.runtracker.ui.run.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quyenln.runtracker.R
import com.quyenln.runtracker.data.model.Run
import com.quyenln.runtracker.utils.getFormattedStopWatchTime
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunHolder>() {

    private val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallBack)

    fun summitList(runs: List<Run>) = differ.submitList(runs)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunHolder {
        return RunHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RunAdapter.RunHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.image).into(ivRunImage)
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

    override fun getItemCount(): Int = differ.currentList.size

    inner class RunHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}