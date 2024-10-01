package com.ripenapps.adoreandroid.agora

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ripenapps.adoreandroid.R

class OnClearFromRecentService: Service() {
    private val NOTIFICATION_ID: Int=1
    private val CHANNEL_ID = "conveyr"
    var mBinder: IBinder =LocalBinder()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?) : IBinder {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@OnClearFromRecentService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = intent!!.getStringExtra("from")
        val notificationBuilder: Notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(intent.getStringExtra("name"))
            .setContentText(type)
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(NOTIFICATION_ID, notificationBuilder);

        return START_NOT_STICKY;
    }


    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        /*val from: String = Preference.getPreference(applicationContext, PrefEntity.serviceFrom).toString()

        if (from == "audio_o") {
            if (OutgoingAudioCallActivity().isCallConnected)          //audio outgoing
                OutgoingAudioCallActivity().endCall();
            else
                OutgoingAudioCallActivity().noAnswerCall();

        } else if (from == "audio_i") {
            if (IncomingAudioCallActivity().isConnected)              //audio incoming
                IncomingAudioCallActivity().endCall();
            else {
                IncomingAudioCallActivity().rejectCall()
            }
        } else if (from == "video_i") {                                 //video incoming
            if (IncomingVideoCallActivity().isConnected)
                IncomingVideoCallActivity().endCall();
            else
                IncomingVideoCallActivity().rejectCall();
        } else if (from == "video_o") {                                //video outgoing
            if (OutgoingVideoCallActivity().isCallConnected)
                OutgoingVideoCallActivity().endCall();
            else
                OutgoingVideoCallActivity().noAnswerCall();
        }

        stopSelf()*/
    }
}