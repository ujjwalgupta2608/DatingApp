package com.ripenapps.conveyr.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MessageBroadcast(val listener: (Intent?) -> Unit): BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        listener(p1)
    }
}