package com.quyenln.runtracker.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.quyenln.runtracker.R
import com.quyenln.runtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.quyenln.runtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.quyenln.runtracker.utils.Constants.ACTION_STOP_SERVICE
import com.quyenln.runtracker.utils.Constants.FAST_LOCATION_UPDATE_INTERVAL
import com.quyenln.runtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.quyenln.runtracker.utils.Constants.NOTIFY_ID
import com.quyenln.runtracker.utils.Constants.TIMER_UPDATE_INTERVAL
import com.quyenln.runtracker.utils.createNotificationChannel
import com.quyenln.runtracker.utils.getFormattedStopWatchTime
import com.quyenln.runtracker.utils.hasLocationPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    private var timeRunInSeconds = MutableLiveData<Long>()
    private var isServiceKilled = false

    @Inject
    lateinit var locationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitValues()
        curNotificationBuilder = baseNotificationBuilder
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationStateTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resume service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Pause services")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Services")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        isTimerEnabled = true
        timeStarted = System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(lapTime + timeRun)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        startForeground(NOTIFY_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if (!isServiceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFY_ID, notification.build())
            }
        })
    }

    private fun killService() {
        pauseService()
        isServiceKilled = true
        isFirstRun = true
        postInitValues()
        stopForeground(true)
        stopSelf()
    }

    //update action of notification
    private fun updateNotificationStateTracking(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).also {
                it.action = ACTION_PAUSE_SERVICE
            }
            getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).also {
                it.action = ACTION_START_OR_RESUME_SERVICE
            }
            getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //clear all old action of notification
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!isServiceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFY_ID, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (hasLocationPermissions(this)) {
                val requestUpdate = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FAST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                locationProviderClient.requestLocationUpdates(
                    requestUpdate,
                    locationCallbacks,
                    Looper.getMainLooper()
                )
            }
        } else {
            locationProviderClient.removeLocationUpdates(locationCallbacks)
        }
    }

    private val locationCallbacks = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("location : ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun postInitValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
    }
}
