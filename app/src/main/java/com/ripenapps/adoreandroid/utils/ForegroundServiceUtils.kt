package com.ripenapps.adoreandroid.utils
import android.app.ActivityManager
import android.content.Context
import android.util.Log


object ForegroundServiceUtils {
    fun isForegroundServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
