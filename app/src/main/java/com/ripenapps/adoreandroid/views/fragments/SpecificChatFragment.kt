package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterChatOtherAudioBinding
import com.ripenapps.adoreandroid.databinding.FragmentSpecificChatBinding
import com.ripenapps.adoreandroid.databinding.SideOptionsLayoutBinding
import com.ripenapps.adoreandroid.models.CommonResponseSocket
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.models.response_models.InitiateChatResponse
import com.ripenapps.adoreandroid.models.response_models.ManualUserDataClass
import com.ripenapps.adoreandroid.models.response_models.SendMediaResponse
import com.ripenapps.adoreandroid.models.response_models.SocketOnlineStatusResponse
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media
import com.ripenapps.adoreandroid.models.response_models.carddetails.MediaList
import com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse.ReceivedMediaClass
import com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse.ReceivedMessage
import com.ripenapps.adoreandroid.models.response_models.receiveSingleMessage.ReceivedSingleMessage
import com.ripenapps.adoreandroid.models.response_models.video_call.VideoCallResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.*
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterChatMessages
import dagger.hilt.android.AndroidEntryPoint
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import io.socket.client.Socket
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Runnable
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class SpecificChatFragment : BaseFragment<FragmentSpecificChatBinding>(), AdapterChatMessages.AudioPlayInterface {
    private var backFromCallHistory=false
    private var callType=""
    private var docType: String=""
    private var tempMultipleImageListPosition: Int=0
    var uploadImageOrVideo=""
    var dateToShow=""
    private var receiverId: String=""
    private var roomId: String=""
    private var profileName=""
    private var profileImage=""
    private lateinit var socket: Socket
    private lateinit var adapterChat: AdapterChatMessages    //temporary
    private var previousScreen=""
    private var chatListResponse= ReceivedMessage()
    private var sendMediaLiveData = SingleLiveEvent<Resources<SendMediaResponse>>()
    private val homeViewModel by viewModels<HomeViewModel>()

    private var cameraGalleryCheck = 0   // 1 for camera, 2 for gallery
    private var capturedImageUri: Uri? = null
    private var galleryImagePathList = arrayListOf<String>()
    private val VideoRequestCode = 122
    private var path = ""
    private var saveAt = ""
    private var isUserBlocked=false
    private var userLikedStatus=""
    private var tempMultipleImageList:MutableList<MultipartBody> = mutableListOf()

    private lateinit var videoCallResponse: VideoCallResponse
    private var manualUserDataClass = ManualUserDataClass()
    private var imagerequestcode: Int = 0
    private var imagePath: String = ""
    private var selectGalleryImage=true
    private var backFromVideoCall = false

    private var currentPlayer: MediaPlayer?=null
    private var isPlaying = false
    private var currentPosition = 0
    private lateinit var updateTimeRunnable: Runnable
    private var handler = Handler(Looper.getMainLooper())
    private var playbackPosition=HashMap<Int,Int>()
    private var playButton: ImageView?=null
    private var pauseButton: ImageView?=null
    private var currentAction=""

    private var audioData: IntArray?=null
    private  var formattedTime: String = ""
    private lateinit var output: String
    private var isRecording = false
    private var isRecordingPressed = false
    private lateinit var mediaRecorder: MediaRecorder
    private var elapsedTimeSeconds: Int = 0
    private var waveformUpdateJob: Job? = null
    private var currentAudioPlayingPos: Int=-1
    private lateinit var audioPlayerBinding : AdapterChatOtherAudioBinding


    override fun setLayout(): Int {
        return R.layout.fragment_specific_chat
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun initView(savedInstanceState: Bundle?) {
        previousScreen = SpecificChatFragmentArgs.fromBundle(requireArguments()).previousScreen!!
        receiverId = SpecificChatFragmentArgs.fromBundle(requireArguments()).receiverId!!
        roomId = SpecificChatFragmentArgs.fromBundle(requireArguments()).roomId!!
        setupUi()
//        Log.i("TAG", "onResume today: "+CommonUtils.getFttedDateToday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")+"  yesterday  "+CommonUtils.getFormattedDateYesterday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        if (isAdded) {
            initializeSocket()
        }
        adapterChat = AdapterChatMessages(mutableListOf(), Preferences.getStringPreference(requireContext(), USER_ID), ::getSelectedItem, ::closeKeyboard, this)
        binding.chatRecycler.adapter = adapterChat
        binding.chatRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                checkIfItemVisible(currentAudioPlayingPos) // Check if the item at position 5 is visible
            }
        })
        onClick()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }
    override fun onResume() {
        super.onResume()
        UserPreference.messagedUserId = receiverId
        if (UserPreference.backFromChatMedia || backFromVideoCall || backFromCallHistory){
            backFromVideoCall=false
            backFromCallHistory=false
            UserPreference.backFromChatMedia=false
            onReceiveMessages()
            emitChatMessage()
        }
    }
    private fun checkIfItemVisible(position: Int) {
        val layoutManager = binding.chatRecycler.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        if (position in firstVisibleItemPosition..lastVisibleItemPosition) {
            Log.d("TAG", "Item at position $position is visible.")
        } else {
            if (position!=-1 && currentPlayer?.isPlaying == true){
                currentPlayer?.pause()
                if(::audioPlayerBinding.isInitialized){
                    audioPlayerBinding.playIcon.visibility = View.VISIBLE
                    audioPlayerBinding.pause.visibility = View.GONE
                }
            }
            Log.d("TAG", "Item at position $position is not visible.")
        }
    }
    private fun closeKeyboard(){
        CommonUtils.hideKeyBoard(requireActivity())
    }
    private fun getSelectedItem(mediaType:String, url:String){
        if (mediaType=="image"||mediaType=="video"){
            findNavController().navigate(SpecificChatFragmentDirections.specificChatToShowImageVideo(
                MediaList(mutableListOf( Media(url = url))), mediaType, "", "specificChat"))
        }else{
            openFile(url)
        }
    }
    private fun openFile(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Download the file
                val file = downloadFile(url)
                if (isAdded){
                    // Get the URI of the file using FileProvider
                    val uri: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireActivity().applicationContext.packageName}.provider",
                        file
                    )

                    // Create an intent to open the file
                    val intent = Intent(Intent.ACTION_VIEW)
                    Log.i("TAG", "getMimeType: "+getMimeType(file))
                    Log.i("TAG", "getMimeType: "+getMimeTypeTwo(file))
                    intent.setDataAndType(uri, getMimeTypeTwo(file))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    // Start the intent
                    startActivity(Intent.createChooser(intent, getString(R.string.open_file_with)))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    @Throws(IOException::class)
    private fun downloadFile(fileUrl: String): File {
        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        // Check if the connection was successful
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("${getString(R.string.failed_to_download_file)} ${connection.responseMessage}")
        }
        var f = File("")
        if(isAdded){
            // Create a temporary file
            val tempFile = File.createTempFile("tempfile", getFileExtension(fileUrl), requireContext().cacheDir)
            tempFile.deleteOnExit()

            // Write the file
            connection.inputStream.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
            f=tempFile
        }
        return f
    }
    private fun getFileExtension(url: String): String {
        return url.substring(url.lastIndexOf('.'))
    }
    private fun getMimeTypeTwo(file: File): String {
        val extension = getFileExtension(file.name)
        return when (extension) {
            ".pdf" -> "application/pdf"
            ".doc", ".docx" -> "application/msword"
            ".xls" -> "application/vnd.ms-excel"
            ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
//            ".zip" -> "application/zip"
            else -> "*/*"
        }
    }
    fun hitSendMediaApi(requestBody: MultipartBody){
        try {
            Log.i("TAG", "hitSendMediaApi: $requestBody")
            sendMediaLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    sendMediaLiveData.postValue(
                        Resources.success(
                            Preferences.getStringPreference(requireContext(), TOKEN)?.let {
                                ApiRepository().sendChatMediaApi(
                                    "Bearer $it", requestBody
                                )
                            }
                        )
                    )
                } catch (ex: Exception) {
                    sendMediaLiveData.postValue(Resources.error(ex.localizedMessage, null))
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    private fun setObserver() {
        sendMediaLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    ProcessDialog.dismissDialog(true)
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                tempMultipleImageListPosition++
                                /*Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()*/
                                Log.i("TAG", "mediadata: "+it?.data)

                                var media=ReceivedMediaClass()  //this handled for single image

                                if (uploadImageOrVideo=="image"){
                                    media.type=uploadImageOrVideo
                                    media.image=it.data?.data?.media
                                }else if (uploadImageOrVideo=="video"){
                                    media.type=uploadImageOrVideo
                                    media.video=it.data?.data?.video
                                    media.thumbnail=it.data?.data?.thumb
                                }else if (uploadImageOrVideo=="audio"){ //here is the recorded audio
                                    media.type=uploadImageOrVideo
                                    media.document=it.data?.data?.document
                                }
                                else{   //here comes all picked documents and device picked audio
                                    Log.i("TAG", "docType: $docType")
                                    media.type=docType
                                    media.document=it.data?.data?.document
                                }

                                Log.i("TAG", "setObserver: "+media)


                                val data = JSONObject() //this handled for single image
                                data.put("sender",Preferences.getStringPreference(context, USER_ID))
                                data.put("messageType","file")
                                data.put("receiver",receiverId)
                                data.put("roomId",roomId)
                                data.put("media",Gson().toJson(media))
                                socket.emit("sendMessage", data)
                                if (tempMultipleImageList.size>tempMultipleImageListPosition){
//                                    tempMultipleImageList.removeAt(0)
                                    hitSendMediaApi(tempMultipleImageList[tempMultipleImageListPosition])
//                                    tempMultipleImageListPosition++
                                }
                            }

                            else -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog(true)
                    Log.e("TAG", "initViewModel: ${it.message}")
                }

            }
        }
        homeViewModel.getLikeDislikeLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            Log.i("TAG", "likeDislikeUserObserver: " + it.data.message)
                            onReceiveMessages()
                            emitChatMessage()
                        }

                        else -> {}
                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun setupUi() {
        profileName = SpecificChatFragmentArgs.fromBundle(requireArguments()).profileName!!
        profileImage = SpecificChatFragmentArgs.fromBundle(requireArguments()).profileUrl!!
        binding.name.text = profileName
        Glide.with(requireContext())
            .load(profileImage).placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(binding.profileImage)
        chatTextWatcher()
        output = getOutputFilePath()
    }
    private fun getOutputFilePath(): String {
        return File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recording.m4a").absolutePath
    }
    private fun chatTextWatcher() {
        binding.chatText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                binding.sendMessage.isVisible = binding.chatText.text.toString().isNotEmpty()
                binding.recordButton.isVisible = binding.chatText.text.toString().isEmpty()

                val params = binding.chatTextLayout.layoutParams as RelativeLayout.LayoutParams
                if (binding.chatText.text.toString().isEmpty()){
                    params.addRule(RelativeLayout.START_OF, R.id.recordButton)
                }else{
                    params.addRule(RelativeLayout.START_OF, R.id.sendMessage)
                }
                binding.chatTextLayout.layoutParams = params
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun setLongPress() {
        val detector = GestureDetector(object: GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                currentAction = "isScrolling"
                Log.d("TAG", "SCROLLING")
                return true
            }

            @RequiresApi(Build.VERSION_CODES.S)
            override fun onLongPress(e: MotionEvent) {
                Log.d("TAG", "Long press!")
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), getString(R.string.microphone_permission_is_not_granted), Toast.LENGTH_SHORT).show()
                    return
                }
                longPressAction(true)
                currentAction = "isLongPressed"
                super.onLongPress(e)
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d("TAG", "Single tap detected")
                Toast.makeText(requireContext(), getString(R.string.tap_and_hold_button_to_record_audio), Toast.LENGTH_SHORT).show()
                return super.onSingleTapUp(e)
            }
        })

        val gestureListener = View.OnTouchListener(function = { view, event ->
            detector.onTouchEvent(event)

            if(event.getAction() == MotionEvent.ACTION_UP) {
                when (currentAction) {
                    "isScrolling" -> {
                        Log.d("TAG", "Done scrolling")
                        currentAction = ""
                    }
                    "isLongPressed" -> {
                        Log.d("TAG", "Done long pressing")
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(requireContext(), getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                        }else{
                            longPressAction(false)
                            currentAction = ""
                        }
                    }
                }

            }

            false
        })

        binding.recordButton.setOnTouchListener(gestureListener)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun longPressAction(status:Boolean) {
        val params = binding.chatTextLayout.layoutParams as RelativeLayout.LayoutParams
        binding.chatTextLayout.layoutParams = params
        if (status){
            binding.chatText.isVisible=false
            binding.recordingLayout.isVisible=true
            startRecording()
        }else{
            binding.deleteAudio.isVisible=true
            binding.mediaImage.isVisible=false
            params.addRule(RelativeLayout.END_OF, R.id.deleteAudio)
            params.addRule(RelativeLayout.START_OF, R.id.sendMessage)
            binding.chatTextLayout.layoutParams = params
            binding.waveForSeekBar.progress=0F
            stopRecording()
        }
    }

    private fun initializeSocket() {
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
        if (roomId!=""||!roomId.isNullOrEmpty()) {
            onReceiveMessages()
            emitChatMessage()
        } else{
            emitInitiateChat()
        }
        onGetRoomId()
        emitOnlineStatusSocket()
        onSocketValidation()
        onBlockUserSuccess()
        onUnblockUserSuccess()
        onUnblockError()
       /* socket.on("userOnline", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG userOnline", data1.toString())
                        var isUserOnlineResponse=Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            IsUserOnlineResponse::class.java)
                        if (isUserOnlineResponse.isOnline=="1"){

                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
        socket.on("userOffline", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG userOffline", data1.toString())

                    } catch (ex: JSONException) {

                    }
                }
            }
        })*/
    }

    private fun setUserBlocked(status:Boolean){
        if (status){
            isUserBlocked=true
            binding.bottomButtonLayout.isVisible=true
            binding.bottomButton.text = getString(R.string.unblock)
            binding.sendMessageLayout.isVisible=false
            enableCallingButtons(false)
        } else{
            isUserBlocked=false
            binding.bottomButtonLayout.isVisible=false
            binding.sendMessageLayout.isVisible=true
            enableCallingButtons(true)
        }
    }

    private fun enableCallingButtons(status: Boolean) {
        if (status){
            binding.videoImage.setOnClickListener {
                callType="videoCall"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Camera")
                    }else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Microphone")
                    }else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Notification")
                    }else{
                        initCall()
                    }
                }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Camera")
                    }else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Microphone")
                    }else{
                        initCall()
                    }
                }
            }
            binding.audioImage.setOnClickListener {
                callType="audioCall"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Microphone")
                    }else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Notification")
                    }else{
                        initCall()
                    }
                }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                        requestPermissionFromSettings("Microphone")
                    }else{
                        initCall()
                    }
                }
            }
        }
        /*else{
            binding.videoImage.isClickable = false
            binding.audioImage.isClickable=false
        }*/
    }

    private fun initCall() {
        manualUserDataClass.userName = profileName
        manualUserDataClass.image = profileImage
        onInvalidData()
        generateAgoraToken()
    }

    private fun requestPermissionFromSettings(permissionType: String) {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context?.packageName, null)
                    intent.data = uri
                    context?.startActivity(intent)
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.permission),
            when (permissionType) {
                "Camera" -> getString(R.string.you_need_to_unable_the_camera_permission_from_settings)
                "Microphone" -> getString(R.string.you_need_to_unable_the_microphone_permission_from_settings)
                "Notification" -> getString(R.string.you_need_to_unable_the_notification_permission_from_settings)
                else -> ""
            },
            getString(R.string.open_settings),
            getString(R.string.no)
        )
    }

    private fun setUserLiked(status:String){
        if (status=="myConnection"){
            binding.bottomButtonLayout.isVisible=false
            binding.sendMessageLayout.isVisible=true
            enableCallingButtons(true)
        } else if (status=="dislike"){
            binding.bottomButtonLayout.isVisible=true
            binding.bottomButton.text = getString(R.string.like)
            binding.sendMessageLayout.isVisible=false
            enableCallingButtons(false)
        } else if (status=="like"||status==""){
            Toast.makeText(requireContext(), getString(R.string.you_are_not_in_connection), Toast.LENGTH_SHORT).show()
            binding.bottomButtonLayout.isVisible=true
            binding.bottomButton.text = getString(R.string.unlike)
            binding.sendMessageLayout.isVisible=false
            enableCallingButtons(false)
        }
    }
    private fun setYouAreBlocked() {
        binding.bottomButtonLayout.isVisible=false
        binding.sendMessageLayout.isVisible=false
        enableCallingButtons(false)
    }
    private fun onReceiveMessages() {
        socket.on("receiveMessages", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG", "chatlist: "+data1)
                    try {
                        Log.i("TAG socket data", data1.toString())
                        chatListResponse = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            ReceivedMessage::class.java)
                        if (chatListResponse.status==200){
                            userLikedStatus=chatListResponse.like_status!!
                            if (chatListResponse.blockUser.contains(receiverId)){
                                setUserBlocked(true)
                            } else if (chatListResponse.blockUser.contains(Preferences.getStringPreference(requireContext(), USER_ID))){
                                Toast.makeText(requireContext(), "${getString(R.string.you_are_blocked_by)} $profileName", Toast.LENGTH_SHORT).show()
                                setYouAreBlocked()
                            }else if (chatListResponse.like_status.equals("myConnection")){
                                setUserLiked(chatListResponse.like_status!!)
                                CommonUtils.showKeyBoard(requireActivity(), binding.chatText)
                                onReceiveMessage()
                            } else if (chatListResponse.like_status.equals("dislike")){
                                setUserLiked(chatListResponse.like_status!!)
                            } else if (chatListResponse.like_status.equals("like")||chatListResponse.like_status.equals("")){
                                setUserLiked(chatListResponse.like_status!!)
                            }
                            chatListResponse.result.forEach { chatMessage ->
                                if (CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "dd MMM yyyy")!=dateToShow){
                                    dateToShow=CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "dd MMM yyyy")
                                    if (dateToShow==CommonUtils.getFormattedDateToday("dd MMM yyyy")){
                                        chatMessage.dateToShow=getString(R.string.today)
                                    } else if (dateToShow==CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
                                        chatMessage.dateToShow=getString(R.string.yesterday)
                                    } else{
                                        chatMessage.dateToShow=dateToShow
                                    }
                                }
                            }
                            if (!chatListResponse.result.isNullOrEmpty()&&chatListResponse.result.size>0){
                                adapterChat.updateList(chatListResponse.result)
                                binding.chatRecycler.smoothScrollToPosition(chatListResponse.result.size-1)
                            }
                        }
                    } catch (ex: JSONException) {

                    }
                }
                socket.off("receiveMessages")

            }
        })
    }

    private fun onGetRoomId() {
        socket.on("getRoomId", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG getRoomId data", data1.toString())

                    try {
                        Log.i("TAG getRoomId data", data1.toString())
                        roomId = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            InitiateChatResponse::class.java).roomId.toString()
                        onReceiveMessages()
                        emitChatMessage()
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onUnblockError() {
        socket.on("unblockUserError", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG unblockUserSuccess", data1.toString())

                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onUnblockUserSuccess() {
        socket.on("unblockUserSuccess", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
//
                        Log.i("TAG unblockUserSuccess", data1.toString())
                        var commonResponseSocket=Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            CommonResponseSocket::class.java)
                        if (commonResponseSocket.status==200){
                            setUserBlocked(false)
                            onReceiveMessages()
                            emitChatMessage()
                            Toast.makeText(requireContext(), commonResponseSocket.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onBlockUserSuccess() {
        socket.on("blockUserSuccess", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG blockUserSuccess", data1.toString())
                        var commonResponseSocket=Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            CommonResponseSocket::class.java)
                        if (commonResponseSocket.status==200){
                            socket.off("receiveMessage")
//                            setUserBlocked(true)
                            onReceiveMessages()
                            emitChatMessage()
                            Toast.makeText(requireContext(), commonResponseSocket.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onInvalidData() {
        socket.on("invalidData", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG onInvalidData", data1.toString())

                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onReceiveMessage() {
        socket.on("receiveMessage", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG receiveMessage data", data1.toString())

                    try {
                        var receivedSingleMessage: ReceivedSingleMessage = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            ReceivedSingleMessage::class.java)
                        if (receivedSingleMessage.status==200){
                            emitChatMessage()
                            if (roomId==receivedSingleMessage.result.roomId!!){
                                roomId = receivedSingleMessage.result.roomId!!
                                if (CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.createdAt!!, "dd MMM yyyy")!=dateToShow){
                                    if (CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.createdAt!!, "dd MMM yyyy")!=dateToShow){
                                        dateToShow=CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.createdAt!!, "dd MMM yyyy")
                                        if (dateToShow==CommonUtils.getFormattedDateToday("dd MMM yyyy")){
                                            receivedSingleMessage.result.dateToShow=getString(R.string.today)
                                        } else if (dateToShow==CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
                                            receivedSingleMessage.result.dateToShow=getString(R.string.yesterday)
                                        } else{
                                            receivedSingleMessage.result.dateToShow=dateToShow
                                        }
                                    }
                                    adapterChat.updateMessage(receivedSingleMessage.result)
                                    adapterChat.notifyDataSetChanged()
                                    if (chatListResponse.result.size>0){
                                        binding.chatRecycler.smoothScrollToPosition(chatListResponse.result.size-1)
                                    }

                                }else{
                                    adapterChat.updateMessage(receivedSingleMessage.result)
                                    if (chatListResponse.result.size>0){
                                        binding.chatRecycler.smoothScrollToPosition(chatListResponse.result.size-1)
                                    }                            }
                                if (receivedSingleMessage.result.isOnline=="1"){
                                    binding.onlineText.text=getString(R.string.online)
                                } else{
                                    if (CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.lastseen!!, "dd MMM yyyy")==CommonUtils.getFormattedDateToday("dd MMM yyyy")){
                                        binding.onlineText.text="${getString(R.string.last_seen_today_at)} "+CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.lastseen!!, "hh:mm a")
                                    } else if(CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.lastseen!!, "dd MMM yyyy")==CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
                                        binding.onlineText.text="${getString(R.string.last_seen_yesterday_at)} "+CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.lastseen!!, "hh:mm a")
                                    } else{
                                        binding.onlineText.text="${getString(R.string.last_seen_on)} "+CommonUtils.convertTimestampToAndroidTime(receivedSingleMessage.result.lastseen!!, "dd MMM yyyy")
                                    }
                                }
                            }

                        } else if (receivedSingleMessage.status==403){
                            Toast.makeText(requireContext(), receivedSingleMessage.message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun onSocketValidation() {
        socket.on("validation", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    Log.i("TAG validation data", data1.toString())

                    try {

                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun emitChatMessage() {
        if (isAdded){
            val data = JSONObject()
            data.put("loginId", Preferences.getStringPreference(requireContext(), USER_ID))
            data.put("roomId", roomId)
            data.put("receiver",receiverId)
            socket.emit("chatMessage", data)
        }
    }
    private fun emitOnlineStatusSocket(){
        val data = JSONObject()
        data.put("id",receiverId)
        socket.emit("status", data)
        onOnlineStatus()
    }
    private fun onOnlineStatus(){
        socket.on("statusMessages", fun(args: Array<Any?>){
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG statusMessages", data1.toString())
                        var onlineStatusResponse=Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            SocketOnlineStatusResponse::class.java)
                        if (onlineStatusResponse.status==200){
                            if (onlineStatusResponse.online=="1"){
                                binding.onlineText.text=getString(R.string.online)
                            } else{
                                if (CommonUtils.convertTimestampToAndroidTime(onlineStatusResponse.lastSeen!!, "dd MMM yyyy")==CommonUtils.getFormattedDateToday("dd MMM yyyy")){
                                    binding.onlineText.text="${getString(R.string.last_seen_today_at)} "+CommonUtils.convertTimestampToAndroidTime(onlineStatusResponse.lastSeen!!, "hh:mm a")
                                } else if(CommonUtils.convertTimestampToAndroidTime(onlineStatusResponse.lastSeen!!, "dd MMM yyyy")==CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
                                    binding.onlineText.text="${getString(R.string.last_seen_yesterday_at)} "+CommonUtils.convertTimestampToAndroidTime(onlineStatusResponse.lastSeen!!, "hh:mm a")
                                } else{
                                    binding.onlineText.text="${getString(R.string.last_seen_on)} "+CommonUtils.convertTimestampToAndroidTime(onlineStatusResponse.lastSeen!!, "dd MMM yyyy")
                                }
                            }
                        }

                    } catch (ex: JSONException) {

                    }
                }
            }
            socket.off("statusMessages")
        })
    }
    private fun popUp(view: View): Boolean {
        val sideOptionsLayoutBinding: SideOptionsLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.side_options_layout,
            null,
            false
        )
        val popWindow = PopupWindow(
            sideOptionsLayoutBinding.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popWindow.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        popWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popWindow.width = 400
        popWindow.elevation = 10f
//        popWindow.showAsDropDown(imageView,100,20)
        if (Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)!="ar"){
            popWindow.showAtLocation(view, Gravity.TOP, 250, 300)
        }else{
            popWindow.showAtLocation(view, Gravity.TOP, -100, 300)
        }
        sideOptionsLayoutBinding.shareProfile.text = getString(R.string.call_history)
        sideOptionsLayoutBinding.disLike.text = "${getString(R.string.media)}     "
        sideOptionsLayoutBinding.third.isVisible=true
        sideOptionsLayoutBinding.fourth.isVisible=true
        sideOptionsLayoutBinding.third.text = getString(R.string.clear_chat)
        if (userLikedStatus=="dislike"){
            sideOptionsLayoutBinding.fourth.text = "${getString(R.string.like)}    "
        }else{
            sideOptionsLayoutBinding.fourth.text = "${getString(R.string.unlike)}    "
        }
        if (!isUserBlocked){
            sideOptionsLayoutBinding.block.text = "${getString(R.string.block)}     "
        }else{
            sideOptionsLayoutBinding.block.text = "${getString(R.string.unblock)}   "
        }

        sideOptionsLayoutBinding.shareProfile.setOnClickListener {
            popWindow.dismiss()
            backFromCallHistory=true
            findNavController().navigate(SpecificChatFragmentDirections.specificChatToCallHistory(receiverId, profileImage, profileName, roomId))
        }
        sideOptionsLayoutBinding.disLike.setOnClickListener {
            popWindow.dismiss()
            UserPreference.backFromChatMedia=true
            socket.off("receiveMessage")
            findNavController().navigate(SpecificChatFragmentDirections.specificChatToShowChatMedia(roomId, receiverId))
        }
        sideOptionsLayoutBinding.third.setOnClickListener {
            popWindow.dismiss()

            createYesNoDialog(
                object : AppDialogListener {
                    override fun onPositiveButtonClickListener(dialog: Dialog) {
                        val data = JSONObject()
                        data.put("userId",Preferences.getStringPreference(requireContext(), USER_ID))
                        data.put("roomId",roomId)
//        Log.i("TAG", "sendTextMessage: $sendMessageRequest")
                        socket.emit("clearChat", data)
                        adapterChat.updateList(mutableListOf())
                        dateToShow=""
                        dialog.dismiss()
                    }

                    override fun onNegativeButtonClickListener(dialog: Dialog) {
                        dialog.dismiss()
                    }
                },
                requireContext(),
                getString(R.string.clear_this_chat),
                getString(R.string.also_delete_media_received_in_this_chat_from_the_device_gallery),
                getString(R.string.clear_chat),
                getString(R.string.cancel),
                2
            )

        }
        sideOptionsLayoutBinding.fourth.setOnClickListener {
            popWindow.dismiss()
            openLikeUnlikePopup()

        }
        sideOptionsLayoutBinding.block.setOnClickListener {
            popWindow.dismiss()

            if (!isUserBlocked){
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            blockUser()
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    "${getString(R.string.block)} $profileName",
                    getString(R.string.are_you_sure_you_want_to_block_this_user),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )
            } else{
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            unblockUser()
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    "${getString(R.string.unblock)} $profileName",
                    getString(R.string.are_you_sure_you_want_to_unblock_this_user),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )
            }

        }
        return true
    }

    private fun openLikeUnlikePopup() {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    if (userLikedStatus=="myConnection"||userLikedStatus=="like"||userLikedStatus==""){
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                homeViewModel.hitLikeDislikeUserApi(
                                    it,
                                    LikeDislikeRequest("dislike"),
                                    receiverId
                                )
                            }
                    } else{
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                homeViewModel.hitLikeDislikeUserApi(
                                    it,
                                    LikeDislikeRequest("like"),
                                    receiverId
                                )
                            }
                    }
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            if(userLikedStatus=="myConnection"||userLikedStatus=="like"||userLikedStatus=="") "${getString(R.string.unlike)} $profileName" else "${getString(R.string.like)} $profileName",
            if(userLikedStatus=="myConnection"||userLikedStatus=="like"||userLikedStatus=="") "${getString(R.string.are_you_sure_you_want_to_dislike_the_user)}" else "${getString(R.string.are_you_sure_you_want_to_like_this_user)}",
            getString(R.string.yes),
            getString(R.string.no),
            2
        )
    }
    private fun unblockUser() {
        val data = JSONObject()
        data.put("userId",receiverId)
        data.put("roomId",roomId)
//        Log.i("TAG", "sendTextMessage: $sendMessageRequest")
        socket.emit("unblockUser", data)
    }
    private fun blockUser() {
        val data = JSONObject()
        data.put("userId",receiverId)
        data.put("roomId",roomId)
//        Log.i("TAG", "sendTextMessage: $sendMessageRequest")
        socket.emit("blockUser", data)
    }
    private fun generateAgoraToken() {
        Log.i("TAG", "generateAgoraToken:11 ")
        if (::socket.isInitialized){
            Log.i("TAG", "generateAgoraToken:22 ")
            val data = JSONObject()
            try {
                data.put("messageType", callType)
                data.put("receiverId", receiverId)
                data.put("roomId", roomId)
                data.put("senderId", Preferences.getStringPreference(context, USER_ID))
//                data.put("messageType", callType)
                data.put("message", callType)
//                data.put("uid", "0")
                socket.emit("agoraToken", data)
                socket.on("agoraToken", fun(args: Array<Any?>) {
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            val agoraToken = args[0] as JSONObject
                            try {
                                Log.i("TAG chatListDataMessages121221", agoraToken.toString())
                                Log.e("TAG", "chatListDataMessages121221:${agoraToken}")
                                videoCallResponse = Gson().fromJson(
                                    JSONArray().put(agoraToken)[0].toString(),
                                    VideoCallResponse::class.java
                                )
                                backFromVideoCall=true
                                socket.off("receiveMessage")
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    if (callType=="videoCall"){
                                        findNavController().navigate(
                                            SpecificChatFragmentDirections.specificChatToOutgoingVideoCallActivity(
                                                Gson().toJson(videoCallResponse), getString(
                                                    R.string.outgoing_video
                                                ), Gson().toJson(manualUserDataClass)
                                            )
                                        )
                                    }else{
                                        findNavController().navigate(
                                            SpecificChatFragmentDirections.actionSpecificChatFragmentToOutgoingAudioCallActivity(
                                                Gson().toJson(videoCallResponse), getString(
                                                    R.string.outgoing_Audio
                                                ), Gson().toJson(manualUserDataClass)
                                            )
                                        )
                                    }
                                }
                                socket.off("agoraToken")
                            } catch (ex: JSONException) {
                                ex.printStackTrace()
                            }
                        }
                    }
                })
            } catch (ex: JSONException) {
            }
        }
    }
    private fun askPermission() {
        val permission: Array<String?> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            else arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (CommonUtils.requestPermissionsNew(requireActivity(), 100, permission)) {
            imagerequestcode = 1
            openCameraGalleryBottomSheet()
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun onClick() {
        binding.deleteAudio.setOnClickListener {
            setChatEditText()
        }
        binding.recordButton.setOnClickListener {
            setLongPress()
        }

        binding.profileCardView.setOnClickListener {
            findNavController().navigate(SpecificChatFragmentDirections.specificChatToUserDetail("5", "specificChatProfile", receiverId))
        }
        binding.bottomButton.setOnClickListener {
            if (isUserBlocked){
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            unblockUser()
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    "${getString(R.string.unblock)} $profileName",
                    getString(R.string.are_you_sure_you_want_to_unblock_this_user),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )

            }else if(userLikedStatus=="dislike"){
                openLikeUnlikePopup()
            } else if (userLikedStatus=="myConnection"||userLikedStatus=="like"||userLikedStatus==""){
                openLikeUnlikePopup()
            }
        }
        binding.optionsImage.setOnClickListener {
            popUp(binding.optionsImage)
        }
        binding.mediaImage.setOnClickListener {
            askPermission()
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.sendMessage.setOnClickListener {
            if (isRecordingPressed){
                setChatEditText()
                isRecordingPressed=false
                uploadImageOrVideo = "audio"

                val builder = MultipartBody.Builder()
                builder.setType(MultipartBody.FORM)
                val file = File(output)
                builder.addFormDataPart(
                    "document",
                    "${System.currentTimeMillis()}_${file.name}",
                    file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                )
                val requestBody = builder.build()
                hitSendMediaApi(requestBody)
            }else{
                if (binding.chatText.text.toString().isNullOrEmpty()){
                    Toast.makeText(requireContext(), getString(R.string.please_enter_something), Toast.LENGTH_SHORT).show()
                }else{
                    sendTextMessage()
                }
            }
//            }
        }
    }

    private fun setChatEditText() {
        binding.deleteAudio.isVisible=false
        binding.mediaImage.isVisible=true
        val params = binding.chatTextLayout.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.END_OF, R.id.mediaImage)
        params.addRule(RelativeLayout.START_OF, R.id.recordButton)
        binding.chatTextLayout.layoutParams = params
        binding.apply {
            waveForSeekBar.apply {
                waveForSeekBar.progress= 0F
            }
        }
        binding.chatText.isVisible=true
        binding.sendMessage.isVisible=false
        binding.recordButton.isVisible=true
        binding.recordingLayout.isVisible=false
    }

    private fun sendTextMessage() {
        val data = JSONObject()
        data.put("message",binding.chatText.text.toString().trim())
        data.put("sender",Preferences.getStringPreference(context, USER_ID))
        data.put("messageType","text")
        data.put("receiver",receiverId)
        data.put("roomId",roomId)
        socket.emit("sendMessage", data)
        binding.chatText.setText("")
    }
    fun getFileExtensionAudio(file: File): String? {
        return file.extension.takeIf { it.isNotEmpty() }
    }
    fun getMimeType(file: File): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
    private fun sendImageMessage(galleryImagePathList:ArrayList<String>) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        for ((index, path) in galleryImagePathList?.withIndex()!!) {    // this list handled for multiple images
            val partName = "media"
            Log.i("TAG", "hitUploadStoryApi: $path")
            val file = File(path)
            builder.addFormDataPart(
                partName,
                "${System.currentTimeMillis()}_$index${file.name}",
                file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
            )
        }
//        Log.i("TAG", "sendImageMessage: media object  "+Gson().toJson(media))
        val requestBody = builder.build()
        hitSendMediaApi(requestBody)
    }
    private fun emitInitiateChat(){
//        onReceiveMessage()
        val data = JSONObject()
        data.put("message","")
        data.put("sender",/*Preferences.getStringPreference(requireContext(), USER_ID)*/ Preferences.getStringPreference(context, USER_ID))
        data.put("messageType","")
        data.put("receiver",receiverId)
        socket.emit("initiateChat", data)
    }
    private fun openCameraGalleryBottomSheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val camera = view1.findViewById<View>(R.id.camera) as TextView
        val gallery = view1.findViewById<View>(R.id.gallery) as TextView
        val document = view1.findViewById<View>(R.id.document) as TextView
        document.isVisible=true    //set to true to open document select coloumn
        title.text = getString(R.string.send_media)
        camera.setOnClickListener {
            cameraGalleryCheck = 1
            bottom.dismiss()
            openPhotoVideoBottomSheet()
        }

        gallery.setOnClickListener {
            cameraGalleryCheck = 2
            bottom.dismiss()
            openPhotoVideoBottomSheet()
        }
        document.setOnClickListener {
            bottom.dismiss()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*" // Set the MIME type to specify the types of files to be displayed

// Specify the MIME types for PDF, Word, Spreadsheet, and Zip files
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf", // PDF files
                "application/msword", // Word documents
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // Word documents (docx)
                "application/vnd.ms-excel", // Spreadsheet files
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // Spreadsheet files (xlsx)
//                "application/zip", // Zip files
                "text/plain",
                "audio/mpeg", // MP3 files
                "audio/x-wav", // WAV files
                "audio/ogg", // OGG files
                "audio/mp4" // M4A files
            ))

            startActivityForResult(intent, 111)
        }
        bottom.show()
    }
    private fun openPhotoVideoBottomSheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val photo = view1.findViewById<View>(R.id.camera) as TextView
        val video = view1.findViewById<View>(R.id.gallery) as TextView
        if (cameraGalleryCheck == 1) {
            title.text = getString(R.string.click)
            photo.text = getString(R.string.photo)
            video.text = getString(R.string.video)
        } else {
            title.text = getString(R.string.open)
            photo.text = getString(R.string.photos)
            video.text = getString(R.string.videos)
        }
        photo.setOnClickListener {
            if (cameraGalleryCheck == 1) {
                openCameraImages()
            } else {
                selectGalleryImage=true
                openGalleryImage()
            }
            bottom.dismiss()
        }

        video.setOnClickListener {
            if (cameraGalleryCheck == 1) {
                openCameraVideos()
            } else {
                selectGalleryImage=false
                openGalleryVideo()
            }
            bottom.dismiss()
        }
        bottom.show()
    }
    private fun openCameraImages() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fileName = "temp.jpg"
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            capturedImageUri =
                requireActivity().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri)
            startActivityForResult(takePictureIntent, 2)


//        }
    }
    private fun openGalleryImage() {
        galleryImagePathList.clear()
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startForImageGallery.launch(intent)
    }

    private fun openCameraVideos() {
            val videoCapture = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoCapture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            videoCapture.putExtra(MediaStore.Video.Thumbnails.HEIGHT, 320)
            videoCapture.putExtra(MediaStore.Video.Thumbnails.WIDTH, 240)
            val maxVideoSize = (50 * 1024 * 1024).toLong() // 50 MB
            videoCapture.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize)
            startActivityForResult(videoCapture, VideoRequestCode)
//        }
    }
    private fun openGalleryVideo() {
        galleryImagePathList.clear()
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startForImageGallery.launch(intent)
    }
    private val startForImageGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) {
                    tempMultipleImageList.clear()
                    for (i in 0 until data.clipData!!.itemCount) {
                        if (galleryImagePathList.size>5){
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.can_upload_upto_five_images_at_a_time),
                                Toast.LENGTH_SHORT
                            ).show()
                            break
                        }
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        addMediaRequestBody(imageUri)
                    }
                } else {
                    val fileUri = data?.data!!
                    if (fileUri?.path?.isNotEmpty() == true) {

                        tempMultipleImageList.clear()
                        addMediaRequestBody(fileUri)
                    }
                }
                if (selectGalleryImage){
                    tempMultipleImageListPosition=0
                    hitSendMediaApi(tempMultipleImageList[tempMultipleImageListPosition])
                }
            }
        }

    private fun addMediaRequestBody(imageUri: Uri) {
        if (imageUri != null && CommonUtils.isImageUri(
                requireContext().contentResolver,
                imageUri
            )
        ) {
            imagePath = java.lang.String.valueOf(
                CommonUtils.createFileSmall(
                    CommonUtils.getRealPathFromDocumentUri(
                        requireContext(),
                        imageUri
                    ), requireContext()
                )
            )
        } else {
            imagePath = CommonUtils.getFilePath(requireContext(), imageUri)!!
        }

        val fileSize = CommonUtils.checkVideoFileSize(imagePath)

        if (fileSize < 100) {
            galleryImagePathList.add(imagePath)
        } else { }
        if (selectGalleryImage){
            if(isAdded){
                uploadImageOrVideo="image"
                val builder = MultipartBody.Builder()
                builder.setType(MultipartBody.FORM)
//                for ((index, path) in arrayOf(path).withIndex()!!) {    // this list handled for multiple images
                val partName = "media"
                Log.i("TAG", "hitSendImageApi: $imagePath")
                val file = File(imagePath)
                builder.addFormDataPart(
                    partName,
                    "${System.currentTimeMillis()}_${file.name}",
                    file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                )
                val requestBody = builder.build()
                tempMultipleImageList.add(requestBody)
            }
        }else{
            if (isAdded){
                val mp = MediaPlayer.create(requireContext(), imageUri)
                if (mp != null) {
                    val duration = mp.duration
                    if (duration > 30000) {
                        Toast.makeText(
                            context,
                            getString(R.string.please_select_videos_less_than_30_secs),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        compressvideo(imageUri)
                    }
                } else {
                    Snackbar.make(
                        binding.topLayout,
                        getString(R.string.unsupported_file_format),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    fun uploadVideo(path:Uri?):Boolean{
        val mp = MediaPlayer.create(requireContext(), path)
        if (mp != null) {
            val duration = mp.duration
            if (duration > 30000) {
                Toast.makeText(
                    context,
                    getString(R.string.please_select_videos_less_than_30_secs),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            else {
                return true
            }
        } else {
            Snackbar.make(
                binding.name,
                getString(R.string.unsupported_file_format),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        return false
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {//camera_img
            if (resultCode == Activity.RESULT_OK) {
                if(isAdded){
                    path = ImageUtils.getInstant().createFileSmall(
                        ImageUtils.getInstant().getRealPathFromUri_(
                            requireContext(),
                            capturedImageUri
                        ), requireContext()
                    ).absolutePath
                    uploadImageOrVideo="image"
                    sendImageMessage(arrayListOf(path))
                }
            }
        } else if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {//gallery_img
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            if (!list.isNullOrEmpty()&&list.size>0){
                tempMultipleImageList.clear()
                Log.i("TAG", "listmediaa: "+list!!.size+"   "+list)
                list?.forEach {
//                    Handler().postDelayed({
                    if(isAdded){
                        val path = ImageUtils.getInstant().createFileSmall(
                            ImageUtils.getInstant().getRealPathFromUri_(
                                requireContext(), it
                            ), requireContext()
                        ).absolutePath
                        uploadImageOrVideo="image"
                        val builder = MultipartBody.Builder()
                        builder.setType(MultipartBody.FORM)
//                for ((index, path) in arrayOf(path).withIndex()!!) {    // this list handled for multiple images
                        val partName = "media"
                        Log.i("TAG", "hitSendImageApi: $path")
                        val file = File(path)
                        builder.addFormDataPart(
                            partName,
                            "${System.currentTimeMillis()}_${file.name}",
                            file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                        )
                        val requestBody = builder.build()
                        tempMultipleImageList.add(requestBody)
                    }
                }
                tempMultipleImageListPosition=0
                hitSendMediaApi(tempMultipleImageList[tempMultipleImageListPosition])
            }


        } else if (requestCode == VideoRequestCode) { //camera_video
            if (resultCode == Activity.RESULT_OK && data!!.data != null) {
                Log.d("MYVideo", Gson().toJson(data!!.data!!))

                val version = Build.VERSION.SDK_INT //version 30 camera video issue
                if (version > 29) {
                    saveAt = ""
                } else {
                    saveAt = Environment.DIRECTORY_DOWNLOADS
                }
//                showProgress()

                if (isAdded){
                    VideoCompressor.start(
                        context = requireContext(), // => This is required
                        uris = listOf(data!!.data!!), // => Source can be provided as content uris
                        isStreamable = true,
                        saveAt = saveAt,
                        listener = object : CompressionListener {
                            override fun onProgress(index: Int, percent: Float) {
                                // Update UI with progress value
                                requireActivity().runOnUiThread {
                                    ProcessDialog.showDialog(requireActivity(), true)

                                    Log.d("MYVideo", "Progresss")
                                }
//                            showProgress()
                            }

                            override fun onStart(index: Int) {
                                // Compression start
                                Log.d("MYVideo", "Start")
//                            showProgress()
                                ProcessDialog.dismissDialog(true)

                            }

                            override fun onSuccess(index: Int, size: Long, path: String?) {
                                ProcessDialog.dismissDialog(true)
                                // On Compression success
                                if (uploadVideo(data?.data!!)){
                                    val builder = MultipartBody.Builder()
                                    builder.setType(MultipartBody.FORM)
                                    val file = File(path)
                                    builder.addFormDataPart(
                                        "video",
                                        "${System.currentTimeMillis()}_$index${file.name}",
                                        file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                                    )
                                    val retriever = MediaMetadataRetriever()
                                    try {
                                        retriever.setDataSource(path)

                                        // Get the first frame (thumbnail) of the video
                                        val bitmap = retriever.frameAtTime

                                        // Convert the Bitmap to a byte array
                                        val byteArray =
                                            bitmap?.let { CommonUtils.bitmapToByteArray(it) }

                                        // Now you can send the byteArray to your API
                                        byteArray?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                                            ?.let {
                                                builder.addFormDataPart(
                                                    "thumbnail",
                                                    file.name,
                                                    it
                                                )
                                            }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        retriever.release()
                                    }
                                    val requestBody = builder.build()
//                            sendVideoMessage()
                                    uploadImageOrVideo="video"
                                    hitSendMediaApi(requestBody)
                                }
                                Log.d("MYVideoCompress", "Succeess  $size " + path)


                            }

                            override fun onFailure(index: Int, failureMessage: String) {
                                // On Failure
//                            hideProgress()
                                ProcessDialog.dismissDialog(true)
                                Log.d("MYVideo", "Failuure")

                            }

                            override fun onCancelled(index: Int) {
                                // On Cancelled
//                            hideProgress()
                                ProcessDialog.dismissDialog(true)
                                Log.d("MYVideo", "Cancel")

                            }

                        },
                        configureWith = Configuration(
                            quality = VideoQuality.VERY_HIGH,
                            frameRate = 10, /*Int, ignore, or null*/
                            isMinBitrateCheckEnabled = true,

                            )
                    )
                }

            }
        } else if (requestCode.toString() == FilePickerConst.REQUEST_CODE_MEDIA_DETAIL.toString()) { //gallery_vdo
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            list?.forEach {
                if (isAdded){
                    val path = ContentUriUtils.getFilePath(requireContext(), it)
                    val mp = MediaPlayer.create(requireContext(), it)
                    if (mp != null) {
                        val duration = mp.duration
                        if (duration > 30000) {
                            Toast.makeText(
                                context,
                                getString(R.string.please_select_videos_less_than_30_secs),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
//                    showProgress()
                            compressvideo(it)
                        }
                    } else {
                        Snackbar.make(
                            binding.topLayout,
                            getString(R.string.unsupported_file_format),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
        else if (requestCode==111){
            Log.i("TAG", "onActivityResult: "+data)
            data?.data?.let { uri ->
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            sendFile(uri)
                            dialog.dismiss()
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    getString(R.string.are_you_sure),
                    "${getString(R.string.do_you_really_want_to_send_the_selected_file_to)} ${profileName}.",
                    getString(R.string.yes),
                    getString(R.string.no)
                )
            }
        }
    }

    private fun sendFile(uri: Uri) {
        val mimeType = requireActivity().contentResolver.getType(uri)
        if (mimeType != null) {
            docType = when {
                mimeType.startsWith("application/pdf") -> "pdf"
                mimeType.startsWith("application/msword") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> "word"
                mimeType.startsWith("application/vnd.ms-excel") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> "excel"
//                        mimeType.startsWith("application/zip") -> "zip"
                mimeType.startsWith("text/plain") -> "text"
                mimeType.startsWith("audio/mpeg") || mimeType.startsWith("audio/x-wav") || mimeType.startsWith("audio/ogg") || mimeType.startsWith("audio/mp4") -> "audio"
                else -> "UNKNOWN"
            }
            var mimeType = when {
                mimeType.startsWith("application/pdf") -> "pdf"
                mimeType.startsWith("application/msword")  -> "doc"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") -> "docx"
                mimeType.startsWith("application/vnd.ms-excel") -> "xls"
                mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") -> "xlsx"
//                        mimeType.startsWith("application/zip") -> "zip"
                mimeType.startsWith("text/plain") -> "text"
                mimeType.startsWith("audio/mpeg") -> "mp3"
                mimeType.startsWith("audio/x-wav") -> "wav"
                mimeType.startsWith("audio/ogg") -> "ogg"
                mimeType.startsWith("audio/mp4") -> "m4a"
                else -> "UNKNOWN"
            }
            // Now you have the type of the selected document (PDF, DOC, SPREADSHEET, ZIP)
            uploadImageOrVideo="document"
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            val file = CommonUtils.getFileFromUri(requireContext(), uri)
            Log.i("TAG", "onActivityResult11: "+mimeType)
            builder.addFormDataPart(
                "document",
                "${System.currentTimeMillis()}_${file.name}.${mimeType}",
                file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
            )
            val requestBody = builder.build()
            hitSendMediaApi(requestBody)
        }
    }

    private fun compressvideo(filePath: Uri) {
        if (isAdded){
            VideoCompressor.start(
                context = requireContext(), // => This is required
                uris = listOf(filePath), // => Source can be provided as content uris
                isStreamable = true,
                saveAt = saveAt,

                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        // Update UI with progress value
                        requireActivity().runOnUiThread {
//                        showProgress()
                        }
                    }

                    override fun onStart(index: Int) {
                        // Compression start
                        Log.d("MYVideo", "Start")
//                    showProgress()

                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        // On Compression success
                        Log.d("MYVideoCompress", "Succeess  $size " + path)
                        val builder = MultipartBody.Builder()
                        builder.setType(MultipartBody.FORM)
                        val file = File(path)
                        builder.addFormDataPart(
                            "video",
                            "${System.currentTimeMillis()}_$index${file.name}",
                            file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
                        )
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(path)

                            // Get the first frame (thumbnail) of the video
                            val bitmap = retriever.frameAtTime

                            // Convert the Bitmap to a byte array
                            val byteArray =
                                bitmap?.let { CommonUtils.bitmapToByteArray(it) }

                            // Now you can send the byteArray to your API
                            byteArray?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                                ?.let {
                                    builder.addFormDataPart(
                                        "thumbnail",
                                        file.name,
                                        it
                                    )
                                }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            retriever.release()
                        }
                        val requestBody = builder.build()
                        uploadImageOrVideo="video"
                        hitSendMediaApi(requestBody)
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        // On Failure
//                    hideProgress()
                        Log.d("MYVideo", "Failuure")
                    }

                    override fun onCancelled(index: Int) {
                        // On Cancelled
//                    hideProgress()
                        Log.d("MYVideo", "Cancel")
                    }

                },
                configureWith = Configuration(
                    quality = VideoQuality.VERY_HIGH,
                    frameRate = 5, //Int, ignore, or null
                    isMinBitrateCheckEnabled = false,
                    videoBitrate = 0, //Int, ignore, or null
                    disableAudio = false, //Boolean, or ignore
                    keepOriginalResolution = false, //Boolean, or ignore
                    videoWidth = 360.0,// Double, ignore, or null
                    videoHeight = 480.0 //Double, ignore, or null
                )

            )
        }
    }

    override fun copyText(text: String) {
        setClipboard(text)
    }
    private fun setClipboard(text: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.text = text
        } else {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.copied_text), text)
            clipboard.setPrimaryClip(clip)
        }
//        Toast.makeText(requireContext(), "Selected message text is copied.", Toast.LENGTH_SHORT).show()

    }
    override fun onPlayOtherAudio(binding: AdapterChatOtherAudioBinding, s: String, position: Int, prevActivePos:Int) {
        if(currentPlayer!=null){
            currentPosition = 0
            isPlaying=false
            binding.playIcon.visibility=View.VISIBLE
            binding.pause.visibility=View.GONE
            currentPlayer?.stop()
            currentPlayer?.release()
            handler.removeCallbacksAndMessages(null)
        }
        currentAudioPlayingPos=position
        audioPlayerBinding = binding
        val mediaPlayer = MediaPlayer()
        playAudio(s,binding.playIcon,binding.idSeekBar,binding.playerTime, mediaPlayer, binding.pause,position, prevActivePos)
        updateTimeRunnable = Runnable {
            updateSeekBar(binding.idSeekBar,mediaPlayer,position)
            handler.postDelayed(updateTimeRunnable, 1000)
        }
        currentPlayer=mediaPlayer
        playButton=binding.playIcon
        pauseButton=binding.pause
        updateButtonVisibility(position, prevActivePos)
    }

    override fun onPauseOtherAudio(binding: AdapterChatOtherAudioBinding, s: String, position: Int, prevActivePos:Int) {
        currentAudioPlayingPos=-1
        pauseAudio(binding.playIcon, binding.idSeekBar, binding.playerTime, currentPlayer!!, binding.pause, position, prevActivePos)
    }
    private fun updateButtonVisibility(activePosition: Int, prevActivePos:Int) {
        val layoutManager = binding.chatRecycler.layoutManager as LinearLayoutManager
        layoutManager.findViewByPosition(activePosition)?.findViewById<ImageView>(R.id.playIcon)?.visibility = View.GONE
        layoutManager.findViewByPosition(activePosition)?.findViewById<ImageView>(R.id.pause)?.visibility = View.VISIBLE
        if (prevActivePos!=-1&&prevActivePos!=activePosition) {
            layoutManager.findViewByPosition(prevActivePos)?.findViewById<ImageView>(R.id.playIcon)?.visibility = View.VISIBLE
            layoutManager.findViewByPosition(prevActivePos)?.findViewById<ImageView>(R.id.pause)?.visibility = View.GONE
        }
    }

    private fun playAudio(output: String, playIcon: ImageView, idSeekBar: SeekBar, playerTime: TextView, mediaPlayer: MediaPlayer, pause: ImageView, position: Int, prevActivePos:Int) {

        try {
            val audioPath = output
            mediaPlayer.reset()
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                val startPosition = playbackPosition[position]
                if (startPosition != null) {
                    if (startPosition > 0 && startPosition < mediaPlayer.duration) {
                        mediaPlayer.seekTo(startPosition)
                    } else {
                        mediaPlayer.seekTo(0)
                    }
                }
                mediaPlayer.start()
                idSeekBar.max = mediaPlayer.duration
                handler.post(updateTimeRunnable)
                mediaPlayer.setOnCompletionListener {
                    pauseAudio(playIcon,idSeekBar,playerTime, mediaPlayer,pause,position, prevActivePos)
                    idSeekBar.progress = 0
                    playbackPosition[position]=0
                }
            }
            playSeekBar(idSeekBar, playerTime, mediaPlayer,position)
            Toast.makeText(requireContext(), getString(R.string.recording_playing), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.error_recording_playing), Toast.LENGTH_SHORT).show()
        }
    }
    private fun playSeekBar(idSeekBar: SeekBar, playerTime: TextView, mediaPlayer: MediaPlayer, position: Int) {
        idSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                try {
                    if (mediaPlayer.isPlaying) {
//                        mediaPlayer.seekTo(progress)
                        updateTextViewCurrentTime(progress, playerTime)
                    }
                } catch (ex: IllegalStateException) {
                    ex.printStackTrace()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(updateTimeRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.post(updateTimeRunnable)
            }
        })
    }
    private fun pauseAudio(playIcon: ImageView, idSeekBar: SeekBar, playerTime: TextView, mediaPlayer: MediaPlayer, pause: ImageView, position: Int, prevActivePos:Int) {
        Toast.makeText(requireContext(), getString(R.string.recording_pause), Toast.LENGTH_SHORT).show()
        mediaPlayer.pause()
        isPlaying = false
        updateButtonVisibility(position, prevActivePos)
        playIcon.visibility = View.VISIBLE
        pause.visibility = View.GONE
        updateSeekBar(idSeekBar, mediaPlayer, position)
        handler.removeCallbacks(updateTimeRunnable)
        playbackPosition[position] = mediaPlayer.currentPosition

    }
    private fun updateSeekBar(idSeekBar: SeekBar, mediaPlayer: MediaPlayer, position: Int) {
        if (mediaPlayer.isPlaying) {
            currentPosition = mediaPlayer.currentPosition
            idSeekBar.progress = currentPosition
        }
    }
    private fun updateTextViewCurrentTime(progress: Int, playerTime: TextView) {
        val minutes = progress / 1000 / 60
        val seconds = (progress / 1000) % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        playerTime.text = formattedTime
    }
    override fun onDestroyView() {
        super.onDestroyView()
        if(currentPlayer!=null){
            currentPlayer!!.pause()
            currentPlayer!!.stop()
            currentPlayer!!.release()
            currentPlayer=null
            handler.removeCallbacksAndMessages(null)
        }

    }
    override fun onPause() {
        super.onPause()
        UserPreference.messagedUserId = ""
//        releaseAudioFocus()
        currentPlayer?.pause()
        if(playButton!=null){
            playButton!!.visibility=View.VISIBLE
            pauseButton!!.visibility=View.GONE
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        binding.time.text = "00:00"
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setOutputFile(output)

        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            isRecording = true
            isRecordingPressed=true
            startWaveformVisualization()
            startTimer()
            Toast.makeText(requireContext(), getString(R.string.recording_started), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "${getString(R.string.failed_to_start_recording)}: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
//            stopRecording()
        }

    }
    private fun stopRecording() {
        if (isRecording) {
            try {
                Log.i("TAG", "stopRecording: "+"try")
                if (::mediaRecorder.isInitialized){
                    mediaRecorder.stop()
                    mediaRecorder.release()
                }
                binding.recordButton.isVisible=false
                binding.sendMessage.isVisible=true
            } catch (e: RuntimeException) {
                Log.i("TAG", "stopRecording: "+"catch")
                e.printStackTrace()
                Toast.makeText(requireContext(), "${getString(R.string.recording_time_is_too_short)}!", Toast.LENGTH_SHORT).show()
                setChatEditText()
                isRecordingPressed=false
            } finally {
                Log.i("TAG", "stopRecording: "+"finally")
                isRecording = false
                waveformUpdateJob?.cancel()
                stopTimer()
            }
        } else {
            //   Toast.makeText(requireContext(), "Recording is not started yet!", Toast.LENGTH_SHORT).show()
        }
    }




    private fun startTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isRecording) {
                updateTimerText()
                delay(1000)
                elapsedTimeSeconds++
            }
        }
    }

    private fun stopTimer() {
        elapsedTimeSeconds = 0
//        binding.time.text = "00:00"
    }

    private fun updateTimerText() {
        val minutes = elapsedTimeSeconds / 60
        val seconds = elapsedTimeSeconds % 60
        formattedTime = String.format("%02d:%02d", minutes, seconds)
        binding.time.text = formattedTime
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startWaveformVisualization() {
        waveformUpdateJob = GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                updateWaveform()
                delay(1000) // Adjust delay as needed
            }
        }
    }

    private suspend fun updateWaveform() {
        withContext(Dispatchers.Main) {
            audioData = readAudioDataFromFile(output)
            audioData?.let {
                binding.waveForSeekBar.setSampleFrom(audioData!!)
            }
        }
    }

    fun readAudioDataFromFile(filePath: String): IntArray? {
        val audioData = mutableListOf<Int>()

        try {
            val fileInputStream = FileInputStream(filePath)
            val dataInputStream = DataInputStream(fileInputStream)
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)
            var bytesRead = 0
            while (dataInputStream.read(buffer).also { bytesRead = it } != -1) {
                for (i in 0 until bytesRead / 2) {
                    val sample = ((buffer[i * 2 + 1].toInt() shl 8) or (buffer[i * 2].toInt() and 0xFF)).toShort()
                    audioData.add(sample.toInt())
                }
            }

            dataInputStream.close()
            fileInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return audioData.toIntArray()
    }
}