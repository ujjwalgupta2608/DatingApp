package com.ripenapps.adoreandroid.agora.singlevideo

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.agora.GlobalSettings
import com.ripenapps.adoreandroid.agora.OnClearFromRecentService
import com.ripenapps.adoreandroid.agora.SoundPoolManager
import com.ripenapps.adoreandroid.databinding.ActivityOutgoingVideoCallBinding
import com.ripenapps.adoreandroid.models.response_models.ManualUserDataClass
import com.ripenapps.adoreandroid.models.response_models.video_call.VideoCallResponse
import com.ripenapps.adoreandroid.utils.AppStateLiveData
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.CommonUtils.isAppOnForeground
import com.ripenapps.adoreandroid.utils.CommonUtils.isBluetoothConnectd
import com.ripenapps.adoreandroid.utils.CommonUtils.isForegroundServiceRunning
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.broadcast.CallEndBroadcast
import com.google.gson.Gson
import com.ripenapps.adoreandroid.agora.audio.OutgoingAudioCallActivity
import com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse.ReceivedMessage
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class OutgoingVideoCallActivity :  AppCompatActivity(), CallEndBroadcast.CallEndCallback {

    private var receiverId: String=""
    private lateinit var serviceIntent: Intent
    private var globalSettings: GlobalSettings = GlobalSettings()

    //Do not move the code structure
    private lateinit var noAnswerTimmer: CountDownTimer
    private lateinit var mBinding: ActivityOutgoingVideoCallBinding
    private lateinit var agoraToken: String
    private lateinit var channelName: String
    private lateinit var callUserImage: String
    private lateinit var callUserName: String
    private var currentUserId = ""
    private var handler: Handler? = null
    private var msgId = ""
    private var mRtcEngine: RtcEngine? = null
    private lateinit var mSocket: Socket
    private val argumentData: OutgoingVideoCallActivityArgs by navArgs()
    var isCallConnected = false
    private var isAudioMute = false
    private var isVideoMute = false
    private var roomId = ""
    private lateinit var callEndBroadcast: CallEndBroadcast
    private var isCallEnd = false
    private var isDestroy = false
    private var isCameraSwitched=false
    private var isSpeakerOn=true
    private val backgroundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isDestroy = true
            finish()
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onError(err: Int) {
            Log.i(
                TAG,
                String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err))
            )
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.i(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid))
        }
        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.i(TAG, "onUserJoined->$uid")
            isCallConnected = true
            mBinding.callStatus = getString(R.string.call_connected)
            startChronometer(mBinding.callTime)
            stopRinging()
            showRemoteVideo(uid)
            noAnswerTimmer.cancel()
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))
            isCallEnd=true
            isCallConnected=false
            handler?.post {
                disconnectRinging()
                if (mRtcEngine != null) {
                    mRtcEngine?.setupRemoteVideo(
                        VideoCanvas(
                            null,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid
                        )
                    )
                }
                finish()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val audioPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        if (audioPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "RECORD_AUDIO permission is granted")
        } else {
            Log.d("Permission", "RECORD_AUDIO permission is not granted")
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

//     Preference.setPreference(this@OutgoingVideoCallActivity, PrefEntity.serviceFrom, "video_o")
        handler = Handler(Looper.getMainLooper())
        window.decorView.isVisible = false
        super.onCreate(savedInstanceState)
//        currentUserId = Session.userDetails._id
        callEndBroadcast = CallEndBroadcast(this)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_outgoing_video_call)
        val app = this.application as MyApplication
        mSocket = app.getSocket()
        // mBinding.parent.setPadding(0, getPaddingAccordingToStatusBarHeight(), 0, 0)
        // window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        getArgumentData()
        onClick()
//        val bundle = intent.getBundleExtra("bundle")
//        if (bundle != null) {
//            agoraToken = bundle.getString(AGORA_TOKEN)!!
//            channelName = bundle.getString(CHANNEL_NAME)!!
//            callUserImage = bundle.getString(CALL_USER_IMAGE)!!
//            callUserName = bundle.getString(CALL_USER_NAME)!!
//            msgId = bundle.getString(MSG_ID)!!
//            mBinding.callUserImage = callUserImage
//            mBinding.userName = callUserName
//        }

        initializeChannel()
        joinChannel()
//        startService(Intent())
    }

    private fun getArgumentData() {
        when (argumentData.from) {
            getString(R.string.outgoing_video) -> {
                val data = Gson().fromJson(argumentData.data, VideoCallResponse::class.java)
                val userData = Gson().fromJson(argumentData.userData, ManualUserDataClass::class.java)
                Log.i(TAG, "getArgumentData outgoingcall: "+data+"           and              "+userData)
                agoraToken = data.data.callToken
                channelName = data.data.channelName
                callUserImage = data.data.senderId.image
                callUserName = data.data.senderId.name
                msgId = data.data.chatMessageId
                roomId = data.data.roomId
                receiverId = data.data.receiverId
                mBinding.name.text = userData.userName
            }
        }
    }

    private fun initializeChannel() {
        try {
            startRinging()
            startTimer()
            mRtcEngine = RtcEngine.create(this, getString(R.string.AGORA_APP_ID), mRtcEventHandler)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "initializeChannel: ${e.message}")
            finish()
        }
    }

    private fun joinChannel() {
        // stopRinging()
        showLocalVideo()

        val option = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        // mRtcEngine!!.joinChannel(agoraToken, channelName, "Extra Optional Data", 0, option)
        mRtcEngine?.joinChannel(agoraToken, channelName, 0, option)

    }

    private fun showLocalVideo() {
        val localSurfaceView = RtcEngine.CreateRendererView(baseContext)
        if (mBinding.localFrame.childCount > 0) mBinding.localFrame.removeAllViews()
        mBinding.localFrame.addView(
            localSurfaceView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        mRtcEngine?.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && isBluetoothConnectd()){
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
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine?.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_BROADCASTER)
        mRtcEngine?.enableVideo()
        mRtcEngine?.enableAudio()
        mRtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                getGlobalSettings()?.videoEncodingDimensionObject,
                VideoEncoderConfiguration.FRAME_RATE.valueOf(getGlobalSettings()?.videoEncodingFrameRate!!),
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.valueOf(getGlobalSettings()?.videoEncodingOrientation!!)
            )
        )
        localSurfaceView.setZOrderMediaOverlay(true)
//        mRtcEngine!!.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
    }
    fun getGlobalSettings(): GlobalSettings {
        if (globalSettings == null) {
            globalSettings = GlobalSettings()
        }
        return globalSettings
    }

    private fun showRemoteVideo(uid: Int) {
        handler?.post {
            val remoteSurfaceView = RtcEngine.CreateRendererView(baseContext)
            mBinding.remoteFrame.addView(
                remoteSurfaceView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            mRtcEngine?.setupRemoteVideo(
                VideoCanvas(
                    remoteSurfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    uid
                )
            )
//                mBinding.remoteFrameCardView.isVisible=true
            remoteSurfaceView?.setZOrderMediaOverlay(true)
            showLocalFrameAgain()
        }
    }

    private fun showLocalFrameAgain() {
        val localSurfaceView = RtcEngine.CreateRendererView(baseContext)
        if (mBinding.localFrame.childCount > 0)
            mBinding.localFrame.removeAllViews()
        mBinding.localFrame.addView(
            localSurfaceView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        mRtcEngine?.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
        localSurfaceView.setZOrderMediaOverlay(true)
    }

    private fun endCall() {
        try {
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("receiverId", receiverId)
            data.put("roomId", roomId)
            data.put("messageType", "videoCall")
            Log.i(TAG, "endCall: hitendCall"+Gson().toJson(data))
            mSocket.emit("endedCall", data)
            mSocket.on("endedCall", fun(args: Array<Any?>) {
                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd response:${rejectCall} ")
                        var chatListResponse = Gson().fromJson(
                            JSONArray().put(rejectCall)[0].toString(),
                            ReceivedMessage::class.java)
                        finish()
                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })
            onReceiveMessages()
            /*mSocket.on("receiveMessages", fun(args: Array<Any?>) {

                runOnUiThread {
                    val CallEnd = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd:${CallEnd} ")
                        finish()


                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })*/

        } catch (ex: Exception) {}

        /*    val data = JSONObject()
            try {
                data.put("chatMessageId", msgId)
                data.put("receiverId", currentUserId)
                data.put("roomId", roomId)
                if (mSocket.connected()) {
                    mSocket.emit(CALL_END, data, object : Ack {
                        override fun call(vararg args: Any?) {
                            runOnUiThread {
                                val jsonObject = Gson().toJson(args[0])
                                Log.i(TAG, "endedAudioCall: ${Gson().toJson(jsonObject)}")
                                finish()
                            }
                        }
                    })
                }*//* else toast(getString(R.string.socket_not_connected))*//*
        } catch (e: JSONException) {
            e.printStackTrace()
        }*/
    }
    private fun onMissedCall() {
        mSocket.on("missedCall", fun(args: Array<Any?>) {
            runOnUiThread {
                val noAnswers = args[0] as JSONObject
                try {
                    Log.e("TAG", "missedCall new onReceiveMessages:${noAnswers} ")
                    finish()
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }
            }
        })
    }
    fun noAnswerCall() {
//        onMissedCall()
        onReceiveMessages()
        try {
            val data = JSONObject()
            data.put("receiverId", receiverId/*Preferences.getStringPreference(this, USER_ID)*/)
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("messageType", "videoCall")
            mSocket.emit("noAnswerCall", data)
            Log.i(TAG, "noAnswerCall: inside "+Gson().toJson(data))
        } catch (ex: Exception) {}
//        try {
//            data.put("chatMessageId", msgId)
//            data.put("roomId", roomId)
//            if (mSocket.connected()) {
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
    private fun startRinging() = SoundPoolManager.getInstance(this).playRinging()
    private fun stopRinging() = SoundPoolManager.getInstance(this).stopRinging()
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
    override fun onDestroy() {
        stopRinging()
        disconnectRinging()
        if (!isDestroy && !isAppOnForeground(this@OutgoingVideoCallActivity)) {
            if (!isCallEnd) {
                if (isCallConnected){
                    endCall()
                }
                else
                    noAnswerCall()
            }
        } else if (!isDestroyed)
            finish()
        noAnswerTimmer.cancel()
//        mBinding.callTime.stop()
        if (mRtcEngine != null) {
            mRtcEngine?.leaveChannel()
        }
        handler?.post { RtcEngine.destroy() }
        mRtcEngine = null
        try {
            unregisterReceiver(backgroundReceiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
//        if (!isCallEnd && !isAppOnForeground(this@com.example.servivet.ui.main.agora.video.OutgoingVideoCallActivity))
        stopService(Intent(this@OutgoingVideoCallActivity, OnClearFromRecentService::class.java))
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}
    companion object {
        private const val TAG = "com.example.shindindidatingapp.agora.singlevideo.OutgoingVideoCallActivity"
    }
    override fun onCallEnd() {
        isCallEnd = true
        isCallConnected=false
        stopRinging()
        finish()
    }

    override fun onPause() {
        super.onPause()
        AppStateLiveData.instance.setForegroundState(false);
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
        if (!isCallEnd && !isAppOnForeground(this@OutgoingVideoCallActivity) && !isForegroundServiceRunning(
                this@OutgoingVideoCallActivity,
                OnClearFromRecentService::class.java
            )
        ) {
            serviceIntent = Intent(this, OnClearFromRecentService::class.java)
                .putExtra("name", callUserName)
                .putExtra("from", "Outgoing video call...")
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    private fun onSwitchCameraClicked() {
        mRtcEngine?.switchCamera()
    }

    private fun startTimer() {
        noAnswerTimmer = object : CountDownTimer(40000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (CommonUtils.isInternetAvailable(this@OutgoingVideoCallActivity)){
                    noAnswerCall()
                }else{
                    finish()
                }
            }
        }
        noAnswerTimmer.start()
    }
    private fun onClick() {
        mBinding.cameraSwitch.setOnClickListener {
            if (isCameraSwitched){
                isCameraSwitched=false
//                mBinding.switchCameraText.setTextColor(resources.getColor(R.color.grey_boulder))
//                mBinding.cameraSwitchIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN)
                onSwitchCameraClicked()
            }else{
                isCameraSwitched=true
//                mBinding.switchCameraText.setTextColor(resources.getColor(R.color.theme))
//                mBinding.cameraSwitchIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN)
                onSwitchCameraClicked()
            }
        }
        mBinding.cameraLayout.setOnClickListener {
            if (isVideoMute){
                isVideoMute=false
                mRtcEngine?.enableLocalVideo(true)
                mBinding.videoCallCameraText.setTextColor(resources.getColor(R.color.grey_boulder))
                mBinding.videoCallCameraIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN);
            }else{
                isVideoMute=true
                mRtcEngine?.enableLocalVideo(false)
                mBinding.videoCallCameraText.setTextColor(resources.getColor(R.color.theme))
                mBinding.videoCallCameraIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN);
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
        mBinding.callEnd.setOnClickListener {
            Log.i(TAG, "onClick123: $isCallConnected")
            if (isCallConnected){
//                endCall()
                isCallEnd=true
                isCallConnected=false
                mRtcEngine?.leaveChannel()
                finish()
//                rejectCall()
            } else
                if (CommonUtils.isInternetAvailable(this)){
//                    emitBackgroundCall()
                    noAnswerCall()
                    emitMissedCall()
                }else{
                    finish()
                }
        }
    }

    private fun emitMissedCall() {  //only for iOS to manage screen close after call cut from other side
        try {
            val data = JSONObject()
            data.put("roomId", roomId)
            data.put("id", currentUserId)
            Log.i(TAG, "missedcall: "+Gson().toJson(data))
            mSocket.emit("missedCall", data)
        } catch (ex: Exception) { }
    }

    private fun emitBackgroundCall() {
        try {
            val data = JSONObject()
            data.put("roomId", roomId)
            data.put("id", receiverId)
            mSocket.emit("backgroundCall", data)
            Log.i(TAG, "backgroundCall: inside "+Gson().toJson(data))
        } catch (ex: Exception) {}
    }

    private fun rejectCall() {
        try {
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            Log.i(TAG, "rejectCall: user id"+Preferences.getStringPreference(this, USER_ID))
            Log.i(TAG, "rejectCall: receiver id "+receiverId)
            data.put("rejectedBy", receiverId)
            data.put("messageType", "videoCall")
            mSocket.emit("rejectCall", data)
            mSocket.on("acceptedCall", fun(args: Array<Any?>) {
                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd rejectCall:${rejectCall} ")
                        finish()
                    } catch (ex: JSONException) {
                        ex.printStackTrace()
                    }
                }

            })

        } catch (ex: Exception) {

        }
    }


}