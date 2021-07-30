package com.quyenln.runtracker.utils

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions

fun hasLocationPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else EasyPermissions.hasPermissions(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
}

