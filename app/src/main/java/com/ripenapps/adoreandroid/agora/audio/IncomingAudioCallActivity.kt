package com.ripenapps.adoreandroid.agora.audio


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.agora.OnClearFromRecentService
import com.ripenapps.adoreandroid.agora.SoundPoolManager
import com.ripenapps.adoreandroid.agora.singlevideo.IncomingVideoCallActivity
import com.ripenapps.adoreandroid.databinding.ActivityIncomingAudioCallBinding
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.preferences.UserPreference.AGORA_TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_IMAGE
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.CHANNEL_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.MSG_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.RECEIVER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.ROOM_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.SENDER_ID
import com.ripenapps.adoreandroid.utils.AppStateLiveData
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.CommonUtils.isAppOnForeground
import com.ripenapps.adoreandroid.utils.CommonUtils.startBackgroundMusicService
import com.ripenapps.adoreandroid.utils.CommonUtils.stopBackgroundMusicService
import com.ripenapps.adoreandroid.utils.ForegroundServiceUtils
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.broadcast.CallEndBroadcast
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject

@Suppress("DEPRECATION")
class IncomingAudioCallActivity : AppCompatActivity(), CallEndBroadcast.CallEndCallback {
    private lateinit var serviceIntent: Intent
    private lateinit var mBinding: ActivityIncomingAudioCallBinding
    private lateinit var agoraToken: String
    private lateinit var channelName: String
    private lateinit var callUserImage: String
    private lateinit var callUserName: String
    private lateinit var mSocket: Socket
    private var msgId = ""
    private var currentUserId = ""
    private var mRtcEngine: RtcEngine? = null
    private var isMute = true
    private lateinit var callEndBroadcast: CallEndBroadcast
    var isConnected = false
    var isDisconnected = false
    private lateinit var audioManager: AudioManager
    private var isCallEnd = false
    private var isDestroy = false
    private var handler: Handler? = null
    private var isAutoEnd = true
    private var roomId = ""
    private var receiverId = ""
    private var senderId = ""
    private var isAudioMute = false
    private var isSpeakerOn=true
    private var notificationId=""


    private val backgroundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isDestroy = true
            isAutoEnd = false
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
             AppStateLiveData.instance.setForegroundState(false);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Preference.setPreference(this@IncomingAudioCallActivity, PrefEntity.serviceFrom, "audio_i")
        handler = Handler(Looper.getMainLooper())
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.decorView.isVisible = false
        callEndBroadcast = CallEndBroadcast(this)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_audio_call)
        stopBackgroundMusicService(this)
        //mBinding.parent.setPadding(0, getPaddingAccordingToStatusBarHeight(), 0, 0)
        mBinding.isCallConnected = false
        val app = this.application as MyApplication
        mSocket = app.getSocket()

        startRinging()
        onClick()
        val bundle = intent.getBundleExtra("bundle")
        if (bundle != null) {
            agoraToken = bundle.getString(AGORA_TOKEN)?:""
            channelName = bundle.getString(CHANNEL_NAME)?:""
            callUserImage = bundle.getString(CALL_USER_IMAGE)?:""
            callUserName = bundle.getString(CALL_USER_NAME)?:""
            msgId = bundle.getString(MSG_ID)?:""
            roomId = bundle.getString(ROOM_ID)?:""
            receiverId = bundle.getString(RECEIVER_ID)?:""
            senderId = bundle.getString(SENDER_ID)?:""
            notificationId = bundle.getString(UserPreference.NOTIFICATION_ID)!!
            //Preference.setPreference(this@IncomingAudioCallActivity, PrefEntity.messageId, msgId)
            mBinding.userName = callUserName
            Glide.with(this).load(bundle.getString(CALL_USER_IMAGE)!!).into(mBinding.profileImage)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && CommonUtils.isBluetoothConnectd()){
//            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
            mRtcEngine?.setEnableSpeakerphone(false)
            mBinding.speakerText.setTextColor(resources.getColor(R.color.grey_boulder))
            mBinding.speakerIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN)
            isSpeakerOn=false
        }
        else{
//            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
            mRtcEngine?.setEnableSpeakerphone(true)
            mBinding.speakerText.setTextColor(resources.getColor(R.color.theme))
            mBinding.speakerIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN)
            isSpeakerOn=true
        }
        initializeChannel()
    }


    private fun initializeChannel() {
        try {
            mRtcEngine = RtcEngine.create(this, getString(R.string.AGORA_APP_ID), mRtcEventHandler)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "initializeChannel: ${e.message}")
            finish()
        }
    }


    private fun joinChannel() {
        mRtcEngine?.let {
            it.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            it.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)
            it.setDefaultAudioRoutetoSpeakerphone(false)
            it.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_DEFAULT)
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false


        val option = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        // mRtcEngine!!.joinChannel(agoraToken, channelName, "Extra Optional Data", 0,option)
        if (mRtcEngine != null && agoraToken != null && channelName != null && option != null) mRtcEngine!!.joinChannel(
            agoraToken,
            channelName,
            0,
            option
        )
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
            mBinding.isCallConnected = true
            mBinding.callStatus = getString(R.string.call_connected)
            startChronometer(mBinding.callTime)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined->$uid")
            mRtcEngine!!.monitorBluetoothHeadsetEvent(true)
            mRtcEngine!!.enableAudio()
            stopRinging()
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            mBinding.callStatus = getString(R.string.callended)
            endCall()
            handler!!.post {
                finish()
            }
            disconnectRinging()
        }

        override fun onError(err: Int) {
            Log.e(
                TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
        }
    }

    override fun onDestroy() {
        if (!isAutoEnd) {
            if (!isDestroy && !isAppOnForeground(this@IncomingAudioCallActivity)) {
                if (!isCallEnd) {
                    if (isConnected)
                        endCall()
                    else {
                        rejectCall()
                    }
                }
            } else if (!isDestroyed)
                finish()
        }
        stopRinging()
        mBinding.callTime.stop()
        if (mRtcEngine != null) {
            mRtcEngine!!.leaveChannel()
        }
        handler!!.post { RtcEngine.destroy() }
        mRtcEngine = null
        try {
            unregisterReceiver(backgroundReceiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
        stopService(Intent(this@IncomingAudioCallActivity, OnClearFromRecentService::class.java))
    }

    public fun noAnswerCall() {
        val data = JSONObject()
        try {
            data.put("messageType", "audioCall")
            data.put("msgId", msgId)
            data.put("endedByUserId", currentUserId)
            if (mSocket.connected()) {
                mSocket.emit("noAnswerCall", data, object : Ack {
                    override fun call(vararg args: Any?) {
                        runOnUiThread {
                            val jsonObject = Gson().toJson(args[0])
                            Log.i(TAG, "noAnswerAudioCall: ${Gson().toJson(jsonObject)}")
                            isDisconnected = true
                            isConnected = false
                            finish()
                        }
                    }
                })
            } /*else toast(getString(R.string.socket_not_connected))*/
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun acceptCall() {

        try {
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("messageType", "audioCall")
            mSocket.emit("acceptedCall", data)
            mSocket.on("acceptedCall", fun(args: Array<Any?>) {

                runOnUiThread {
                    val data = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd:${data} ")
                        joinChannel()
                        stopRinging()
                        isConnected = true


                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })

        } catch (ex: Exception) {

        }
    }

    private fun rejectCall() {
        try {
            Log.e("TAG", "rejectCall: emit ")
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("rejectedBy", senderId)
            data.put("messageType", "audioCall")
            mSocket.emit("rejectCall", data)
            mSocket.on("acceptedCall", fun(args: Array<Any?>) {

                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "rejectCall on:${rejectCall} ")
                        isConnected = false
                        isDisconnected = true
                        finish()
                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })

        } catch (ex: Exception) {

        }

    }


    fun endCall() {
        try {
            Log.e("TAG", "endCall: emit ")
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("receiverId", receiverId)
            data.put("messageType", "audioCall")
            data.put("notificationId", notificationId)
            Log.i(TAG, "endCall11: "+Gson().toJson(data))
            mSocket.emit("endedCall", data)
            mSocket.on("endedCall", fun(args: Array<Any?>) {

                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd on:${rejectCall} ")
                        finish()


                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })

        } catch (ex: Exception) {

        }
    }

    private fun startRinging() {
        startBackgroundMusicService(this)
    }

    private fun stopRinging() {
        stopBackgroundMusicService(this)
        /* SoundPoolManager.getInstance(this).stopRinging()*/
    }

    private fun disconnectRinging() = SoundPoolManager.getInstance(this).playDisconnect()

    private fun startChronometer(mChronometer: Chronometer) {
        val stoppedMilliseconds: Int
        mChronometer.base = SystemClock.elapsedRealtime()

        val chronoText = mChronometer.text.toString()
        val array = chronoText.split(":".toRegex()).toTypedArray()

        stoppedMilliseconds = if (array.size == 2) {
            Integer.parseInt((array[0].toInt() * 60 * 1000).toString()) + Integer.parseInt((array[1].toInt() * 1000).toString())
        } else {
            Integer.parseInt((array[0].toInt() * 60 * 60 * 1000).toString()) + Integer.parseInt(
                (array[1].toInt() * 60 * 1000).toString() + Integer.parseInt(
                    (array[2].toInt() * 1000).toString()
                )
            )
        }

        mChronometer.base = SystemClock.elapsedRealtime() - stoppedMilliseconds
        mChronometer.start()

    }

    fun onClick() {
        mBinding.acceptCall.setOnClickListener {
            acceptCall()
        }

        mBinding.rejectCall.setOnClickListener {
            if (isConnected)
                endCall()
            else
                rejectCall()
        }

        mBinding.endCall.setOnClickListener {
            endCall()
        }

        mBinding.mute.setOnClickListener {
            if (isAudioMute){
                isAudioMute=false
                mRtcEngine?.enableLocalAudio(true)
//                mRtcEngine?.muteLocalAudioStream(false)
                mBinding.muteText.setTextColor(resources.getColor(R.color.grey_boulder))
                mBinding.muteIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN);

            }else{
                isAudioMute=true
                mRtcEngine?.enableLocalAudio(false)
//                mRtcEngine?.muteLocalAudioStream(true)
                mBinding.muteText.setTextColor(resources.getColor(R.color.theme))
                mBinding.muteIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN);
            }
        }
        mBinding.speaker.setOnClickListener {
            if (isSpeakerOn){
                isSpeakerOn=false
//                mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
                mRtcEngine?.setEnableSpeakerphone(false)
                mBinding.speakerText.setTextColor(resources.getColor(R.color.grey_boulder))
                mBinding.speakerIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN);
            }else{
                isSpeakerOn=true
//                mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
                mRtcEngine?.setEnableSpeakerphone(true)
                mBinding.speakerText.setTextColor(resources.getColor(R.color.theme))
                mBinding.speakerIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    override fun onCallEnd() {
        isAutoEnd = false
        if (!isDestroy) {
            isCallEnd = true
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        AppStateLiveData.instance.setForegroundState(true);
        currentUserId = Preferences.getStringPreference(this, USER_ID).toString()

        val intentFilter = IntentFilter()
        intentFilter.addAction("callEnd")
        registerReceiver(callEndBroadcast, intentFilter, RECEIVER_NOT_EXPORTED)
        val intentFilterBackground = IntentFilter("app_background_action")
        registerReceiver(backgroundReceiver, intentFilterBackground, RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(callEndBroadcast)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!isCallEnd && !isAppOnForeground(this@IncomingAudioCallActivity) && !ForegroundServiceUtils.isForegroundServiceRunning(
                this@IncomingAudioCallActivity,
                OnClearFromRecentService::class.java
            )
        ) {
            serviceIntent = Intent(this, OnClearFromRecentService::class.java)
                .putExtra("name", callUserName)
                .putExtra("from", "Incoming audio call...")
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}

    companion object {
        private const val TAG = "IncomingAudioCallActivity"
    }
}
