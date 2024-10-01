package com.ripenapps.adoreandroid.utils

import android.content.Intent

import android.util.Log

import com.ripenapps.adoreandroid.data.model.call_module.notification_call.CallBody
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.UserPreference.NOTIFICATION

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class FirebaseServices : FirebaseMessagingService() {
    private var message = ""
    private var title = ""
    private lateinit var image: String
    private var count = 0
    private var notificationData = ""

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("TAG", "onNewToken: $token")
        Preferences.setStringPreference(this, FCM_TOKEN, token)
    }

    override fun onMessageReceived(rMessage: RemoteMessage) {
        super.onMessageReceived(rMessage)
        rMessage.notification?.let { Log.e("TAG", "onMessageReceived: ${Gson().toJson(it)}") }
        val callBody = Gson().fromJson(rMessage.data["customData"], CallBody::class.java)
        Log.e("TAG", "checkCallBody111: ${Gson().toJson(callBody)}")
        if (rMessage.notification?.title != null && rMessage.notification?.title.toString() != "") {
            title = rMessage.notification?.title.toString()
            message = rMessage.notification?.body.toString()
        } else {
            title = rMessage.data["title"].toString()
            message = rMessage.data["message"].toString()
        }

        val intentData = Intent(NOTIFICATION)
        intentData.putExtra(NOTIFICATION, rMessage.notification?.body)
        sendBroadcast(intentData)

        Log.e("TAG", "checkCallBody: ${Gson().toJson(rMessage)}")
        Log.e("TAG", "onMessageReceived: $message")
    }
}