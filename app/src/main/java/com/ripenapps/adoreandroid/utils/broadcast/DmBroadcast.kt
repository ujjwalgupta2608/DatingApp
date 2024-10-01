package com.ripenapps.adoreandroid.utils.broadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent



class DmBroadcast(private val dmNotificationCallback: DmNotificationCallback): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        dmNotificationCallback.onNewDm()
    }

    interface DmNotificationCallback{
        fun onNewDm()
    }
}