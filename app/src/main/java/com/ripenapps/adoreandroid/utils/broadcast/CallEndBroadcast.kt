package com.ripenapps.adoreandroid.utils.broadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallEndBroadcast(private val callEndCallback: CallEndCallback): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        callEndCallback.onCallEnd()
    }
    interface CallEndCallback{
        fun onCallEnd()
    }
}