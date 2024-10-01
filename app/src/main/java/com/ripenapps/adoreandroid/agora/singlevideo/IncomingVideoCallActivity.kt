package com.ripenapps.adoreandroid.agora.singlevideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.agora.GlobalSettings
import com.ripenapps.adoreandroid.agora.OnClearFromRecentService
import com.ripenapps.adoreandroid.agora.SoundPoolManager
import com.ripenapps.adoreandroid.databinding.ActivityIncomingVideoCallBinding
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.AGORA_TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_IMAGE
import com.ripenapps.adoreandroid.preferences.UserPreference.CALL_USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.CHANNEL_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference.MSG_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.RECEIVER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.ROOM_ID
import com.ripenapps.adoreandroid.preferences.UserPreference.SENDER_ID
import com.ripenapps.adoreandroid.utils.AppStateLiveData
import com.ripenapps.adoreandroid.utils.CommonUtils.isAppOnForeground
import com.ripenapps.adoreandroid.utils.CommonUtils.isBluetoothConnectd
import com.ripenapps.adoreandroid.utils.CommonUtils.isForegroundServiceRunning
import com.ripenapps.adoreandroid.utils.CommonUtils.startBackgroundMusicService
import com.ripenapps.adoreandroid.utils.CommonUtils.stopBackgroundMusicService
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.broadcast.CallEndBroadcast
import com.google.gson.Gson
import com.ripenapps.adoreandroid.preferences.UserPreference.NOTIFICATION_ID
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

class IncomingVideoCallActivity : AppCompatActivity(), CallEndBroadcast.CallEndCallback {

    //Do not move the code structure

    private var notificationId=""
    private var senderId: String = ""
    private var globalSettings: GlobalSettings = GlobalSettings()
    private lateinit var serviceIntent: Intent
    private lateinit var mBinding: ActivityIncomingVideoCallBinding
    private lateinit var agoraToken: String
    private lateinit var channelName: String
    private lateinit var callUserImage: String
    private lateinit var callUserName: String
    private var msgId = ""
    private lateinit var mSocket: Socket
    private var mRtcEngine: RtcEngine? = null
    private lateinit var callEndBroadcast: CallEndBroadcast
    private var handler: Handler? = null
    private var isAudioMute = false
    private var isVideoMute = false
    private var isSpeakerOn=true
    private var isCameraSwitched=false
    var isConnected = false
    private var isCallEnd = false
    private var isDestroy = false
    private var isAutoEnd = true
    private var roomId = ""
    private var receiverId = ""

    private val REQUEST_CAMERA_PERMISSION = 200
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraId: String


    private val backgroundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "onReceive: backgroundReceiver")
            /*isDestroy = true
            isAutoEnd = false
            if (isConnected){
                isConnected=false
                endCall()
            }else{
                finish()
            }*/
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onConnectionLost() {
            Log.i(
                TAG,
                "Connection Lost"
            )
        }

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
//                mBinding.isCallConnected = true
            isConnected = true
            mBinding.callStatus = getString(R.string.call_connected)

            /*  stopRinging()*/
            stopBackgroundMusicService(this@IncomingVideoCallActivity)

            startChronometer(mBinding.callTime)

            showRemoteVideo(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason))
            endCall()
            mBinding.callStatus = getString(R.string.callended)
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
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreataaaaae: amanaa")

        handler = Handler(Looper.getMainLooper())
        //Preference.setPreference(this@IncomingVideoCallActivity, PrefEntity.serviceFrom, "video_i")

        callEndBroadcast = CallEndBroadcast(this)
        stopBackgroundMusicService(this)
        /* startRinging()*/
        startBackgroundMusicService(this)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_video_call)
        //  mBinding.parent.setPadding(0, getPaddingAccordingToStatusBarHeight(), 0, 0)
        val app = this.application as MyApplication
        mSocket = app.getSocket()
//        onBackgroundCall()
//        mBinding.isCallConnected = false
        isConnected = false
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        //  window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val bundle = intent.getBundleExtra("bundle")
        if (bundle != null) {
            Log.i(TAG, "onCreataaaaae: amanaa")
            Log.i(TAG, "onCreate: "+Gson().toJson(bundle))
            agoraToken = bundle.getString(AGORA_TOKEN)!!
            channelName = bundle.getString(CHANNEL_NAME)!!
            callUserImage = bundle.getString(CALL_USER_IMAGE)!!
            callUserName = bundle.getString(CALL_USER_NAME)!!
            msgId = bundle.getString(MSG_ID)!!
            roomId = bundle.getString(ROOM_ID)!!
            receiverId = bundle.getString(RECEIVER_ID)!!
            senderId = bundle.getString(SENDER_ID)!!
            notificationId = bundle.getString(NOTIFICATION_ID)!!
            Log.i(TAG, "onCreate: senderId $senderId")
            mBinding.name.text = callUserName
        }
        initializeChannel()
        showLocalVideo()
        mBinding.cameraSwitch.setOnClickListener {
            onSwitchCameraClicked()
        }
        onClick()
        surfaceView = findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView.holder

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            openFrontCamera()
        }

        /*surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (this@IncomingVideoCallActivity::cameraDevice.isInitialized) {
                        startPreview()
                    }
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (this@IncomingVideoCallActivity::cameraDevice.isInitialized) {
                    cameraDevice.close()
                }
            }
        })*/

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
        if (this::cameraCaptureSession.isInitialized) {
            cameraCaptureSession.close()
        }
        val option = ChannelMediaOptions()
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        if (mRtcEngine != null && agoraToken != null && channelName != null && option != null)
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
        Log.i(TAG, "showLocalVideo: "+mRtcEngine)
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
        mRtcEngine?.setEnableSpeakerphone(true)
        mRtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                getGlobalSettings()!!.videoEncodingDimensionObject,
                VideoEncoderConfiguration.FRAME_RATE.valueOf(getGlobalSettings()!!.videoEncodingFrameRate),
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.valueOf(getGlobalSettings()!!.videoEncodingOrientation)
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
//            mBinding.remoteFrameCardView.isVisible=true
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
    private fun onBackgroundCall() {
        mSocket.on("backgroundCall", fun(args: Array<Any?>) {
            runOnUiThread {
                val noAnswers = args[0] as JSONObject
                try {
                    Log.e("TAG", "backgroundCall ${noAnswers} ")
//                    finish()
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }
            }
        })
    }
    private fun acceptCall() {
//        onMissedCall()
        Log.i(TAG, "acceptCall: ")
        try {
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            data.put("messageType", "videoCall")
            mSocket.emit("acceptedCall", data)
            mSocket.on("acceptedCall", fun(args: Array<Any?>) {
                runOnUiThread {
                    val CallEnd = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd:${CallEnd} ")
                        joinChannel()
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
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("roomId", roomId)
            Log.i(TAG, "rejectCall: user id"+Preferences.getStringPreference(this, USER_ID))
            Log.i(TAG, "rejectCall: receiver id "+senderId)
            data.put("rejectedBy", senderId)
            data.put("messageType", "videoCall")
            mSocket.emit("rejectCall", data)
            mSocket.on("acceptedCall", fun(args: Array<Any?>) {
                runOnUiThread {
                    val rejectCall = args[0] as JSONObject
                    try {
                        Log.e("TAG", "CallEnd rejectCall:${rejectCall} ")
                        isConnected=false
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
        Log.i(TAG, "endCall: ")
        try {
            val data = JSONObject()
            data.put("chatMessageId", msgId)
            data.put("receiverId", receiverId)
            data.put("notificationId", notificationId)
            data.put("roomId", roomId)
            data.put("messageType", "videoCall")
            Log.i(TAG, "endCall11: "+Gson().toJson(data))
            mSocket.emit("endedCall", data)
            mSocket.on("endedCall", fun(args: Array<Any?>) {
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
    }

    private fun startRinging() = SoundPoolManager.getInstance(this).playRinging()

    private fun stopRinging() = SoundPoolManager.getInstance(this).stopRinging()

    private fun disconnectRinging() = SoundPoolManager.getInstance(this).playDisconnect()
    private fun onClick() {
        mBinding.cameraSwitch.setOnClickListener {
            if (isCameraSwitched){
                isCameraSwitched=false
                mBinding.switchCameraText.setTextColor(resources.getColor(R.color.grey_boulder))
                mBinding.cameraSwitchIcon.setColorFilter(resources.getColor(R.color.grey_boulder), PorterDuff.Mode.SRC_IN)
                onSwitchCameraClicked()
            }else{
                isCameraSwitched=true
                mBinding.switchCameraText.setTextColor(resources.getColor(R.color.theme))
                mBinding.cameraSwitchIcon.setColorFilter(resources.getColor(R.color.theme), PorterDuff.Mode.SRC_IN)
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
            mRtcEngine?.leaveChannel()
            isConnected=false
            endCall()
        }
        mBinding.rejectCall.setOnClickListener {
            if (this::cameraCaptureSession.isInitialized) {
                cameraCaptureSession.close()
            }
            rejectCall()
        }
        mBinding.acceptCall.setOnClickListener {
            acceptCall()
        }
    }
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
        stopBackgroundMusicService(this)
        if (!isAutoEnd) {
            if (!isDestroy && !isAppOnForeground(this@IncomingVideoCallActivity)) {
                if (!isCallEnd) {
                    if (isConnected){
                        finish()
                    }
                    else
                        rejectCall()
                }
            } else if (!isDestroyed)
                finish()
        }
//        mBinding.callTime.stop()
        if (mRtcEngine != null) mRtcEngine?.leaveChannel()
        handler?.post { RtcEngine.destroy() }
        mRtcEngine = null
        try {
            unregisterReceiver(backgroundReceiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
//        if (!isCallEnd && !isAppOnForeground(this@com.example.servivet.ui.main.agora.video.IncomingVideoCallActivity))
        stopService(Intent(this@IncomingVideoCallActivity, OnClearFromRecentService::class.java));
    }

    /*override fun onPause() {
        super.onPause()
        stopBackgroundMusicService(this)

        if(isConnected)
            endCall()
        else
            rejectCall()

        mBinding.callTime.stop()
        if (mRtcEngine != null) mRtcEngine!!.leaveChannel()
        handler!!.post { RtcEngine.destroy() }
        mRtcEngine = null
    }*/

    override fun onCallEnd() {
        Log.i(TAG, "onCallEnd: hitOnCallEnd")
        isAutoEnd = false
        if (!isDestroy/* && isConnected*/) {
            isCallEnd = true
            if (isConnected){
                isConnected=false
                endCall()
            }else{
                finish()
            }
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

    override fun onPause() {
        super.onPause()
        AppStateLiveData.instance.setForegroundState(false);
        if (this::cameraCaptureSession.isInitialized) {
            cameraCaptureSession.close()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(callEndBroadcast)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!isCallEnd && !isAppOnForeground(this@IncomingVideoCallActivity) && !isForegroundServiceRunning(
                this@IncomingVideoCallActivity,
                OnClearFromRecentService::class.java
            )
        ) {
            serviceIntent = Intent(this, OnClearFromRecentService::class.java)
                .putExtra("name", callUserName)
                .putExtra("from", "Incoming video call...")
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {}

    companion object {
        private const val TAG = "com.example.shindindidatingapp.agora.singlevideo.IncomingVideoCallActivity"
    }

    private fun onSwitchCameraClicked() {
        mRtcEngine?.switchCamera()
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFrontCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun openFrontCamera() {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    this.cameraId = cameraId
                    break
                }
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                          return
            }
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    Log.i(TAG, "onOpened incomm: $camera  ${ (::cameraDevice.isInitialized)}")
                    if (::cameraDevice.isInitialized){
                        try {
                            startPreview()
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    } else{
//                        openFrontCamera()
                    }

                }

                override fun onDisconnected(camera: CameraDevice) {
                    if (::cameraDevice.isInitialized)
                        cameraDevice.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (::cameraDevice.isInitialized)
                        cameraDevice.close()
                    openFrontCamera()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startPreview() {
        val surface = surfaceHolder.surface
        if (!surface.isValid) {
            openFrontCamera()
            // Handle invalid Surface
            return
        }

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@IncomingVideoCallActivity, "Failed to start camera preview", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}