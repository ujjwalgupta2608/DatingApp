package com.ripenapps.adoreandroid.agora.audio

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.*
import android.util.Log
import android.view.*
import android.widget.Chronometer
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.agora.OnClearFromRecentService
import com.ripenapps.adoreandroid.agora.SoundPoolManager
import com.ripenapps.adoreandroid.agora.singlevideo.OutgoingVideoCallActivityArgs
import com.ripenapps.adoreandroid.databinding.ActivityOutgoingAudioCallBinding
import com.ripenapps.adoreandroid.models.response_models.ManualUserDataClass
import com.ripenapps.adoreandroid.models.response_models.video_call.VideoCallResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.utils.AppStateLiveData
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.CommonUtils.isAppOnForeground
import com.ripenapps.adoreandroid.utils.ForegroundServiceUtils
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.broadcast.CallEndBroadcast

import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject

class OutgoingAudioCallActivity : AppCompatActivity(), CallEndBroadcast.CallEndCallback {
    private lateinit var serviceIntent: Intent
    private lateinit var noAnswerTimmer: CountDownTimer
    private lateinit var mBinding: ActivityOutgoingAudioCallBinding
    private lateinit var agoraToken: String
    private lateinit var channelName: String
    private lateinit var callUserImage: String
    private lateinit var callUserName: String
    private var currentUserId = ""
    private val argumentData: OutgoingVideoCallActivityArgs by navArgs()
    private var msgId = ""
    private lateinit var mSocket: Socket
    private var handler: Handler? = null
    private var mRtcEngine: RtcEngine? = null
    private var isCallConnected = false
    private var isMute = true
    private var isCallEnd = false
    private var roomId = ""
    private var receiverId = ""
    private lateinit var callEndBroadcast: CallEndBroadcast
    private var isDestroy = false
    private var senderId: String = ""
    private var isAudioMute = false
    private var isSpeakerOn=true

    private val backgroundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isDestroy = true
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        // Preference.setPreference(this@OutgoingAudioCallActivity, PrefEntity.serviceFrom, "audio_o")
        callEndBroadcast = CallEndBroadcast(this)
        window.decorView.isVisible = false
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_outgoing_audio_call)
        val app = this.application as MyApplication
        mSocket = app.getSocket()
        senderId = Preferences.getStringPreference(this, USER_ID).toString()
        onClick()
        // senderId not initialized
        // mBinding.parent.setPadding(0, getPaddingAccordingToStatusBarHeight(), 0, 0)
        getArgumentData()
        initializeChannel()
        joinChannel()
    }


    private fun getArgumentData() {
        when (argumentData.from) {
            getString(R.string.outgoing_Audio) -> {
                val data = Gson().fromJson(argumentData.data, VideoCallResponse::class.java)
                val userData = Gson().fromJson(argumentData.userData, ManualUserDataClass::class.java)

                Log.e(TAG, "getArgumentData: ${argumentData.userData}", )

                agoraToken = data.data.callToken
                channelName = data.data.channelName
                callUserImage = data.data.senderId.image
                callUserName = data.data.senderId.name
                msgId = data.data.chatMessageId
                roomId = data.data.roomId
                receiverId = data.data.receiverId

                mBinding.userName = userData.userName
                Glide.with(this).load(userData.image).into(mBinding.profileImage)

            }
        }
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
        startRinging()
        startTimer()
        mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine!!.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)
        mRtcEngine!!.setDefaultAudioRoutetoSpeakerphone(false)
        mRtcEngine!!.setAudioProfile(
            Constants.AUDIO_PROFILE_DEFAULT,
            Constants.AUDIO_SCENARIO_DEFAULT
        )
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
        val option = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        mRtcEngine!!.joinChannel(agoraToken, channelName, 0, option)
    }

    private fun startTimer() {
        noAnswerTimmer = object : CountDownTimer(40000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }
            override fun onFinish() {
                noAnswerCall()
            }
        }
        noAnswerTimmer.start()
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
            mRtcEngine!!.enableAudio()
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined->$uid")
            stopRinging()
            noAnswerTimmer.cancel()
            isCallConnected = true
            mBinding.callStatus = getString(R.string.call_connected)
            startChronometer(mBinding.callTime)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))
            mBinding.callStatus = getString(R.string.callended)
            handler!!.post {
                disconnectRinging()
                finish()
            }
        }

        override fun onError(err: Int) {
            Log.e(
                TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
        }

    }


    override fun onDestroy() {
        Log.i(TAG, "onDestroy: calltime ${mBinding.callTime.drawingTime}")
        if (!isDestroy && !isAppOnForeground(this@OutgoingAudioCallActivity)) {
            if (!isCallEnd) {
                if (isCallConnected)
                    endCall()
                else
                    noAnswerCall()
            }
        } else if (!isDestroyed)
            finish()

        stopRinging()
        noAnswerTimmer.cancel()
        disconnectRinging()
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
        stopService(Intent(this@OutgoingAudioCallActivity, OnClearFromRecentService::class.java))
    }

     fun endCall() {
        try {
            Log.i(TAG, "endCall: emit")
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("receiverId", receiverId)
            data.put("messageType", "audioCall")
            mSocket.emit("endedCall", data)
            mSocket.on("endedCall", fun(args: Array<Any?>) {
                Log.i(TAG, "endCall: on")
                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd:${rejectCall} ")
                        finish()
                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })

        } catch (ex: Exception) {

        }


//        Log.i(TAG, "endCall: outgoing")
//
//        val data = JSONObject()
//        try {
//            data.put("msgId", msgId)
//            data.put("endedByUserId", currentUserId)
//            if (mSocket.connected()) {
//                mSocket.emit(END_CALL, data, object : Ack {
//                    override fun call(vararg args: Any?) {
//                        runOnUiThread {
//                            val jsonObject = Gson().toJson(args[0])
//                            Log.i(TAG, "endedAudioCall: ${Gson().toJson(jsonObject)}")
//                            finish()
//                        }
//                    }
//                })
//            } /*else toast(getString(R.string.socket_not_connected))*/
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
    }
    private fun onReceiveMessages() {
        mSocket.on("receiveMessages", fun(args: Array<Any?>) {
            runOnUiThread {
                val noAnswers = args[0] as JSONObject
                try {
                    Log.e("TAG", "CallEnd new onReceiveMessages:${noAnswers} ")
                    finish()
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }
            }
        })
    }
    fun noAnswerCall() {
        onReceiveMessages()
        try {
            Log.i(TAG, "noAnswerCall: emit")
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("rejectedBy", senderId)
            data.put("receiverId", receiverId)
            data.put("messageType", "audioCall")
            Log.i(TAG, "noAnswerCall: "+Gson().toJson(data))
            mSocket.emit("noAnswerCall", data)
//            mSocket.on("noAnswerCall", fun(args: Array<Any?>) {
//                Log.i(TAG, "noAnswerCall: on")
//                runOnUiThread {
//                    val rejectCall = args[0] as JSONObject
//                    try {
//                        Log.e("TAG", "CallEnd:${rejectCall} ")
//                        finish()
//                    } catch (ex: JSONException) {
//                        ex.printStackTrace()
//                    }
//                }
//
//            })

        } catch (ex: Exception) {

        }


//        val data = JSONObject()
//        try {
//            data.put("msgId", msgId)
//            data.put("endedByUserId", currentUserId)
//            if (mSocket.connected()) {
//
//                mSocket.emit(NO_ANSWER_CALL, data, object : Ack {
//                    override fun call(vararg args: Any?) {
//                        runOnUiThread {
//                            val jsonObject = Gson().toJson(args[0])
//                            Log.i(TAG, "noAnswerAudioCall: ${Gson().toJson(jsonObject)}")
//                            finish()
//                        }
//                    }
//                })
//            } /*else toast(getString(R.string.socket_not_connected))*/
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
    }

    private fun startRinging() = SoundPoolManager.getInstance(this).playRinging()

    private fun stopRinging() {
        SoundPoolManager.getInstance(this).stopRinging()
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
        mBinding.endCall.setOnClickListener {
        //       endCall()
            if (isCallConnected) {
//                endCall()
                isCallEnd=true
                isCallConnected=false
                mRtcEngine?.leaveChannel()
                finish()
            }
            else {
                emitMissedCall()
                noAnswerCall()
            }
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

    private fun emitMissedCall() {  //only for iOS to manage screen close after call cut from other side
        try {
            val data = JSONObject()
            data.put("roomId", roomId)
            data.put("id", senderId)
            Log.i(TAG, "missedcall: "+Gson().toJson(data))
            mSocket.emit("missedCall", data)
        } catch (ex: Exception) { }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}

    companion object {
        private const val TAG = "com.ripenapps.adoreandroid.agora.audio.OutgoingAudioCallActivity"
    }

    override fun onCallEnd() {
        if (!isDestroy) {
            isCallEnd = true
            stopRinging()
            noAnswerTimmer.cancel()
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        AppStateLiveData.instance.setForegroundState(true);
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

        if (!isCallEnd && !isAppOnForeground(this@OutgoingAudioCallActivity) && !ForegroundServiceUtils.isForegroundServiceRunning(
                this@OutgoingAudioCallActivity,
                OnClearFromRecentService::class.java
            )
        ) {
            serviceIntent = Intent(this, OnClearFromRecentService::class.java)
                .putExtra("name", callUserName)
                .putExtra("from", "Outgoing audio call...")
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    override fun onPause() {
        super.onPause()
        AppStateLiveData.instance.setForegroundState(false);
    }
}