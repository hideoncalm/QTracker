package com.quyenln.runtracker.utils

import java.util.concurrent.TimeUnit

fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
    var millis = ms
    val h = TimeUnit.MICROSECONDS.toHours(millis)
    millis -= TimeUnit.HOURS.toMillis(h)
    val m = TimeUnit.MICROSECONDS.toMinutes(millis)
    millis -= TimeUnit.MINUTES.toMillis(m)
    val s = TimeUnit.MILLISECONDS.toSeconds(millis)
    if (!includeMillis) {
        return "${if (h < 10) "0" else ""}$h:" +
                "${if (m < 10) "0" else ""}$m:" +
                "${if (s < 10) "0" else ""}$s"
    } else {
        millis -= TimeUnit.SECONDS.toMillis(s)
        millis /= 10
        return "${if (h < 10) "0" else ""}$h:" +
                "${if (m < 10) "0" else ""}$m:" +
                "${if (s < 10) "0" else ""}$s:" +
                "${if (millis < 10) "0" else ""}$millis"
    }
}