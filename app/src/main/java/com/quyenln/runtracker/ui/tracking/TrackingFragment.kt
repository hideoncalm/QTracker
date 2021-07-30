package com.quyenln.runtracker.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.quyenln.runtracker.R
import com.quyenln.runtracker.base.BaseFragment
import com.quyenln.runtracker.data.model.Run
import com.quyenln.runtracker.databinding.FragmentTrackingBinding
import com.quyenln.runtracker.service.Polyline
import com.quyenln.runtracker.service.TrackingService
import com.quyenln.runtracker.ui.main.MainViewModel
import com.quyenln.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.quyenln.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.quyenln.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.quyenln.runtracker.utils.Constants.MAP_ZOOM
import com.quyenln.runtracker.utils.Constants.POLYLINE_COLOR
import com.quyenln.runtracker.utils.Constants.POLYLINE_WIDTH
import com.quyenln.runtracker.utils.getDistanceOfPolyline
import com.quyenln.runtracker.utils.getFormattedStopWatchTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : BaseFragment<FragmentTrackingBinding>() {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var timeCurrentMillis = 0L

    @set:Inject
    var weight = 50f

    override val methodInflate: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTrackingBinding =
        FragmentTrackingBinding::inflate

    override fun initViews(savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
    }

    override fun initListeners() {
        btnToggleRun.setOnClickListener {
            toggleRun()
            btnCancelRun.visibility = View.VISIBLE
        }

        btnCancelRun.setOnClickListener {
            showCancelTrackingDialog()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addTwoLastedPoints()
            moveCameraToUser()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            timeCurrentMillis = it
            if (timeCurrentMillis > 0) {
                btnCancelRun.visibility = View.VISIBLE
            }
            val formatTime = getFormattedStopWatchTime(timeCurrentMillis, true)
            tvTimer.text = formatTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel and delete all the data of this current Run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && timeCurrentMillis > 0L) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += getDistanceOfPolyline(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (((timeCurrentMillis / 1000f) / 60f) / 60f) * 10) / 10
            val timestamp = Calendar.getInstance().timeInMillis
            val calories = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                bitmap,
                timestamp,
                avgSpeed,
                distanceInMeters,
                timeCurrentMillis,
                calories
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Save Success",
                Snackbar.LENGTH_SHORT
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyLine in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyLine)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addTwoLastedPoints() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polyLineOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyLineOption)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}