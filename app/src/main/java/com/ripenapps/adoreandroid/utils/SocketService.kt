package com.ripenapps.adoreandroid.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SocketService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start your socket connection here
        return START_STICKY
    }

    override fun onDestroy() {
        // Close your socket connection here
        super.onDestroy()
    }
}