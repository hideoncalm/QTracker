package com.quyenln.runtracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.quyenln.runtracker.utils.Constants.NOTIFY_CHANNEL_ID
import com.quyenln.runtracker.utils.Constants.NOTIFY_CHANNEL_NAME

fun createNotificationChannel(notificationManager: NotificationManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NOTIFY_CHANNEL_ID,
            NOTIFY_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}

