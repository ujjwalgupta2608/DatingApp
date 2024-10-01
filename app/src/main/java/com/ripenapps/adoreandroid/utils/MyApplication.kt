package com.ripenapps.adoreandroid.utils

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import dagger.hilt.android.HiltAndroidApp
import io.socket.client.IO
import io.socket.client.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@HiltAndroidApp
class MyApplication:Application(), LifecycleObserver {
    private var appContext: Context? = null
    private var wasInBackground = false
    private lateinit var mSocket: Socket
//    private var SOCKET_URL = "http://13.235.137.221:6870"
    private var SOCKET_URL = "https://adore-dating.com:6870"

    override fun onCreate() {
        super.onCreate()
        appContext = this
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initializedSocket()
    }
    private fun initializedSocket() {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }), SecureRandom())

        // Create an OkHttpClient with the custom SSL settings
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            .hostnameVerifier { _, _ -> true } // Trust all hostnames for development purposes
            .build()

        // Configure IO socket options with OkHttp client
        val opts = IO.Options().apply {
            callFactory = okHttpClient
            webSocketFactory = okHttpClient
        }


        mSocket = IO.socket((SOCKET_URL+"?userId="+Preferences.getStringPreference(appContext, USER_ID)), opts)
        mSocket.connect()
    }
    fun getSocket(): Socket {
        return mSocket
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        wasInBackground = true
        try {
            if (!mSocket.connected()) {
                initializedSocket()
            }
        }
        catch (ex:Exception){
            Log.e("TAG", "onMoveToForeground: $ex", )
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onMoveToForegroundResume() {
        // app moved to foreground
        wasInBackground = true
        Log.i("TAG", "onMoveToForegroundResume: ")
        try {
                initializedSocket()
        }
        catch (ex:Exception){
            Log.e("TAG", "onMoveToForeground: $ex", )
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {  //to change the online status
        Log.i("TAG", "onMoveToBackground: ")
        // app moved to background
        wasInBackground = false
        emitDisconnectUser()
    }
    private fun emitDisconnectUser() {
        mSocket.emit("disconnectUser")
    }
}