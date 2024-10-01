package com.ripenapps.adoreandroid.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ripenapps.adoreandroid.data.model.call_module.notification_call.CallBody
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.agora.OnClearFromRecentService
import com.ripenapps.adoreandroid.agora.singlevideo.IncomingVideoCallActivity
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.preferences.UserPreference.AGORA_TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_IMAGE
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.CHANNEL_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.MSG_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.NOTIFICATION
import com.ripenapps.adoreandroid.preferences.UserPreference.RECEIVER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.ROOM_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.SENDER_ID
import com.ripenapps.adoreandroid.utils.CommonUtils.isAppOnForeground
import com.ripenapps.adoreandroid.utils.CommonUtils.isForegroundServiceRunning
import com.ripenapps.adoreandroid.utils.CommonUtils.launchActivityWithBundle
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.ripenapps.adoreandroid.agora.audio.IncomingAudioCallActivity
import com.ripenapps.adoreandroid.preferences.UserPreference.NOTIFICATION_ID
import java.util.*

open class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var notificationData = ""
    private var notificationType: String? = ""
    private lateinit var message: String
    private lateinit var title: String
    private lateinit var image: String
    private var count = 0
    // private lateinit var bitmap: Bitmap

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "onNewToken: $token")
        Preferences.setStringPreference(this, FCM_TOKEN, token)
        Log.e("TAG", "onNewToken1: ${Preferences.getStringPreference(this, FCM_TOKEN)}")
        UserPreference.deviceToken = token
    }

    override fun onMessageReceived(rMessage: RemoteMessage) {
        super.onMessageReceived(rMessage)
        notificationType = rMessage.data["notificationType"]
        rMessage?.let { Log.e("TAG", "d: ${Gson().toJson(it)}") }
        rMessage.notification?.let { Log.e("TAG", "onMessageReceived: notificationkey key ${Gson().toJson(it)}") }
        rMessage.data?.let { Log.e("TAG", "onMessageReceived: datakey ${Gson().toJson(it)}") }
        val callBody = Gson().fromJson(rMessage.data["customData"], CallBody::class.java)
        Log.e("TAG", "onMessageReceived callbody: ${Gson().toJson(callBody)}")
        title = rMessage.data["title"].toString()
        message = rMessage.data["body"].toString()
        Log.i("TAG", "onMessageReceived: "+"data")
        notificationData = Gson().toJson(rMessage.data)

        val intentData = Intent(NOTIFICATION)
        intentData.putExtra(NOTIFICATION, rMessage.data["body"])
        sendBroadcast(intentData)
        Log.i("TAG", "onMessageReceived:123 "+rMessage.data["body"]+"    "+notificationType)
        if ((notificationType == "audioCall"||notificationType == "videoCall")&&(rMessage.data["body"]=="Call ended"||rMessage.data["body"]=="Call rejected")) {
            NotificationManagerCompat.from(applicationContext).cancelAll()
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                if (isForegroundServiceRunning(applicationContext, OnClearFromRecentService::class.java) && !isAppOnForeground(applicationContext)) {
                    val intent = Intent("app_background_action")
                    sendBroadcast(intent)
                } else if (AppStateLiveData.instance.getIsForeground().value != null && !AppStateLiveData.instance.getIsForeground().value!!) {
                    val intent = Intent("app_background_action")
                    sendBroadcast(intent)
                } else {
                    callEnd()
                }
            }
            if (rMessage.data["body"]=="Call rejected"){
                sendNotification1(message, title)
            }
        } else if((notificationType == "audioCall"||notificationType == "videoCall") && rMessage.data["body"]?.contains("missed call") == true){
            NotificationManagerCompat.from(applicationContext).cancelAll()
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                if (isForegroundServiceRunning(applicationContext, OnClearFromRecentService::class.java) && !isAppOnForeground(applicationContext)) {
                    val intent = Intent("app_background_action")
                    sendBroadcast(intent)
                } else if (AppStateLiveData.instance.getIsForeground().value != null && !AppStateLiveData.instance.getIsForeground().value!!) {
                    val intent = Intent("app_background_action")
                    sendBroadcast(intent)
                } else {
                    callEnd()
                    sendNotification1(message, title)
                }
            }
        }
        else if((notificationType == "audioCall"||notificationType == "videoCall") &&rMessage.data["body"]=="Incoming call"){
            Log.i("TAG", "onMessageReceived: "+"incommingcall")
            sendNotification(message, title, count, callBody)
            Log.i("TAG", "dfadauydgasdg: "+"ABCDEF")

            val differenceInSeconds = (System.currentTimeMillis() - rMessage.sentTime) / 1000
            Log.i("TAG", "differenceInSeconds: "+differenceInSeconds)
            if (differenceInSeconds < 30) {
                if (callBody.messageType=="videoCall"){
                    inviteForVideoCall(callBody)
                }else{
                    inviteForAudioCall(callBody)
                }
            }
        }else if(notificationType=="chat"){
            if(UserPreference.isChatFragmentOpen=="1"){
                callEnd()
            }
            if (rMessage.data["userId"]!=UserPreference.messagedUserId){
                Log.i("TAG", "inside chat message")
                sendNotification1(message, title/*, count, callBody*/)
            }

        }else if (notificationType=="card"){
            sendNotification1(message, title/*, count, callBody*/)
        }else if(notificationType=="Admin"){
            sendNotification1(message, title/*, count, callBody*/)
        }
    }

    private fun inviteForAudioCall(callBody: CallBody) {
        val bundle = Bundle()
        bundle.putString(AGORA_TOKEN, callBody.callToken)
        bundle.putString(CHANNEL_NAME, callBody.channelName)
        bundle.putString(CALL_USER_IMAGE, callBody.senderId.image)
        bundle.putString(CALL_USER_NAME, callBody.senderId.name)
        bundle.putString(MSG_ID, callBody.chatMessageId)
        bundle.putString(ROOM_ID, callBody.roomId)
        bundle.putString(RECEIVER_ID, callBody.receiverId)
        bundle.putString(NOTIFICATION_ID, callBody.notificationId)
        bundle.putString(SENDER_ID, callBody.senderId._id)
        launchActivityWithBundle(IncomingAudioCallActivity::class.java, bundle)
    }

    private fun inviteForVideoCall(callBody: CallBody) {
        val bundle = Bundle()
        bundle.putString(AGORA_TOKEN, callBody.callToken)
        bundle.putString(CHANNEL_NAME, callBody.channelName)
        bundle.putString(CALL_USER_IMAGE, callBody.senderId.image)
        bundle.putString(CALL_USER_NAME, callBody.senderId.name)
        bundle.putString(MSG_ID, callBody.chatMessageId)
        bundle.putString(ROOM_ID, callBody.roomId)
        bundle.putString(RECEIVER_ID, callBody.receiverId)
        bundle.putString(NOTIFICATION_ID, callBody.notificationId)
        bundle.putString(SENDER_ID, callBody.senderId._id)
        launchActivityWithBundle(IncomingVideoCallActivity::class.java, bundle)
    }

    private fun callEnd() {
        val callEnd = Intent("callEnd")
        callEnd.`package` = "com.ripenapps.adoreandroid"
        applicationContext.sendBroadcast(callEnd)
    }

    private fun sendNotification(message: String, title: String, count: Int, callBody: CallBody) {
        val pendingIntent: PendingIntent?
        val channelId = "channel-05434377729111997"
        val channelName = "Adore Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, channelName, importance)
            mChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(mChannel)
        }
        // val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val soundUri =
            Uri.parse("android.resource://" + applicationContext.packageName.toString() + "/" + R.raw.incomming)

        try {
            //  defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            val r = RingtoneManager.getRingtone(applicationContext, soundUri)
            // r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder.setSmallIcon(R.mipmap.rect_icon)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(message)
        notificationBuilder.setAutoCancel(true)
//      notificationBuilder.setSound(soundUri)
        notificationBuilder.setChannelId(channelId)
        notificationBuilder.setOngoing(true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL)

//        startBackgroundMusicService(applicationContext)

        Log.e("TAG", "sendNotification: $notificationType")

        val intent: Intent = when (notificationType) {
            "audioCall" -> Intent(baseContext, IncomingAudioCallActivity::class.java)
            "videoCall" -> Intent(baseContext, IncomingVideoCallActivity::class.java)
            else -> Intent()
        }.also {
            val bundle = Bundle()
            bundle.putString(AGORA_TOKEN, callBody.callToken)
            bundle.putString(CHANNEL_NAME, callBody.channelName)
            bundle.putString(CALL_USER_IMAGE, callBody.senderId.image)
            bundle.putString(CALL_USER_NAME, callBody.senderId.name)
            bundle.putString(MSG_ID, callBody.chatMessageId)
            it.putExtra("bundle", bundle)
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.flags = Intent.FLAG_FROM_BACKGROUND
            it.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        }
//        val intent = Intent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {


            pendingIntent = PendingIntent.getActivity(
                this,
                0, intent,
                PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getActivity(
                this,
                0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        //  val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        notificationBuilder.setFullScreenIntent(pendingIntent, true)

        try {
            notificationManager.notify(getRequestCode(), notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun sendNotification1(message: String, title: String) {
        var pendingIntent: PendingIntent?
        val intent = Intent(baseContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationData", notificationData)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "channel-05434377729111997"
        val channelName = "Adore Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, channelName, importance)
            mChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(mChannel)
        }
        val soundUri =
            Uri.parse("android.resource://" + applicationContext.packageName.toString() + "/" + R.raw.incomming)
        try {
            //  defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            val r = RingtoneManager.getRingtone(applicationContext, soundUri)
            // r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        notificationBuilder.setSmallIcon(R.mipmap.rect_icon)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(message)
        notificationBuilder.setAutoCancel(true)
//      notificationBuilder.setSound(soundUri)
        notificationBuilder.setChannelId(channelId)
        notificationBuilder.setOngoing(false)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL)
        notificationBuilder.setFullScreenIntent(pendingIntent, false)  //only this line hidden for pending intent

        try {
            notificationManager.notify(getRequestCode(), notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun handleIntent(intent: Intent?) {
        super.handleIntent(intent)
    }

    private fun getRequestCode(): Int {
        val rnd = Random()
        return 100 + rnd.nextInt(900000)
    }


}
