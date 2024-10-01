package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentChatBinding
import com.ripenapps.adoreandroid.models.response_models.navigationChatList.NavigationChatListResponse
import com.ripenapps.adoreandroid.models.response_models.navigationChatList.Result
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListingResponse
import com.ripenapps.adoreandroid.preferences.CITY
import com.ripenapps.adoreandroid.preferences.COUNTRY
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.PROFILE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.SwipeHelper
import com.ripenapps.adoreandroid.utils.broadcast.CallEndBroadcast
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.StoryViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterChatsListing
import com.ripenapps.adoreandroid.views.adapters.AdapterStoriesRecycler
import com.google.gson.Gson
import com.ripenapps.adoreandroid.models.response_models.ManualUserDataClass
import com.ripenapps.adoreandroid.models.response_models.video_call.VideoCallResponse
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(), AdapterStoriesRecycler.StorySelector, CallEndBroadcast.CallEndCallback {
    private lateinit var socket: Socket
    private val storyViewModel by viewModels<StoryViewModel>()
    private lateinit var adapterStories: AdapterStoriesRecycler
    private lateinit var adapterChatsList:AdapterChatsListing
    private var uploadedStory = false
    private var myStoryImagePath = ""
    private lateinit var storyListingResponse: StoryListingResponse
    private var otherUserSelectedPosition: Int = -1
    private var chatList=NavigationChatListResponse()
    private val CAMERA_MIC_PERMISSION_REQUEST_CODE = 100
    private val OVERLAY_PERMISSION_REQ_CODE = 123
    private lateinit var callEndBroadcast: CallEndBroadcast
    private lateinit var videoCallResponse: VideoCallResponse
    private var manualUserDataClass = ManualUserDataClass()


    override fun setLayout(): Int {
        return R.layout.fragment_chat
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (isAdded) {
           initializeSocket()
        }
        callEndBroadcast = CallEndBroadcast(this)
        initChatsListRecycler()
        onClick()
        requestCameraAndMicrophonePermissions()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Preferences.getStringPreference(requireActivity(), TOKEN)
            ?.let { storyViewModel.hitUserListingApi(it) }
        setObserver()
        if (::socket.isInitialized){
            Log.i("TAG", "initializeSocket:  init")
            emitNavigationList()
            onNavigationListSocket()
        }else{
            Log.i("TAG", "initializeSocket: not init")
        }
    }
    private val backgroundReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }
    override fun onStop() {
        super.onStop()
        try {
            requireActivity().unregisterReceiver(callEndBroadcast)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setLocation()
        UserPreference.isChatFragmentOpen="1"
        val intentFilter = IntentFilter()
        intentFilter.addAction("callEnd")
        requireActivity().registerReceiver(callEndBroadcast, intentFilter,
            Context.RECEIVER_NOT_EXPORTED)
        val intentFilterBackground = IntentFilter("app_background_action")
        requireActivity().registerReceiver(backgroundReceiver, intentFilterBackground,
            Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        UserPreference.isChatFragmentOpen="0"
    }

    override fun onDestroy() {
        super.onDestroy()
        UserPreference.isChatFragmentOpen="0"
    }
    private fun requestCameraAndMicrophonePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE)
            }
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED /*||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED*/){

                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO/*, Manifest.permission.WAKE_LOCK*/),
                    CAMERA_MIC_PERMISSION_REQUEST_CODE)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            requestOverlayPermission()
        } else {
            // Overlay permission is granted
            // Proceed with your application logic
        }
    }
    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:com.ripenapps.adoreandroid")
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            // Check if both permissions are granted
            if (grantResults.size == 3 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            } else if (grantResults.size == 2 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                // Permissions granted, proceed with your functionality
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions required for video call!",
                    Toast.LENGTH_SHORT
                ).show()
                // Permissions not granted, handle accordingly (e.g., show explanation or disable functionality)
            }
        }else  if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(requireContext())) {
                // Overlay permission granted
                // Proceed with your application logic
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions required for video call!",
                    Toast.LENGTH_SHORT
                ).show()
                requestOverlayPermission()
                // Overlay permission not granted
                // You may want to inform the user about the importance of the permission
            }
        }
    }
    private fun onClick() {
        binding.notification.setOnClickListener {
            findNavController().navigate(ChatFragmentDirections.chatToNotification())
        }
        binding.locationText.setOnClickListener {
            findNavController().navigate(ChatFragmentDirections.chatToSearchLocation())
        }
    }
    private fun setLocation() {
        binding.locationText.text = Preferences.getStringPreference(requireContext(), CITY)+", "+Preferences.getStringPreference(requireContext(), COUNTRY)
    }
    private fun openClearChatDialog(position: Int) {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    val data = JSONObject()
                    data.put("userId",Preferences.getStringPreference(requireContext(), USER_ID))
                    data.put("roomId",chatList?.result?.get(position)?.roomId)
                    dialog.dismiss()
                    socket.emit("clearChat", data)
                    if (::socket.isInitialized){
                        emitNavigationList()
                        onNavigationListSocket()
                    }
//                    adapterChatsList.notifyDataSetChanged()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            "Clear this chat?",
            "Also delete media received in this chat from the device gallery",
            "Clear Chat",
            "Cancel",
            2
        )
    }

    private fun initializeSocket() {
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
    }

    private fun emitNavigationList() {
        val data = JSONObject()
        data.put("userId", Preferences.getStringPreference(requireContext(), USER_ID))
        socket.emit("navigationChatList", data)
        Log.i("TAG", "emitNavigationList: "+Gson().toJson(data))
    }

    private fun onNavigationListSocket() {
        socket.on("getNavigationChatList", fun(args: Array<Any?>) {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    val data1 = args[0] as JSONObject
                    try {
                        Log.i("TAG socket data", data1.toString())
                        chatList = Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            NavigationChatListResponse::class.java)
                        adapterChatsList =
                            chatList.result?.let { AdapterChatsListing(::getSelectedChat, it) }!!
                        binding.emptyCardLayout.isVisible = chatList.result?.size == 0
                        binding.chatsListRecycler.adapter = adapterChatsList
                    } catch (ex: JSONException) {

                    }
                }
            }
            socket.off("getNavigationChatList")
        })
    }

    private fun getSelectedChat(position: Int, result:Result){
        findNavController().navigate(ChatFragmentDirections.chatToSpecificChat("chat",
            result.roomId?.trim()!!, result._id!!, result.name!!, result.profile!!
        ))
    }
    private fun initChatsListRecycler() {
        adapterChatsList= AdapterChatsListing(::getSelectedChat, mutableListOf())
        binding.chatsListRecycler.adapter = adapterChatsList
        binding.storiesRecycler.adapter = AdapterStoriesRecycler(this, false, "", listOf(), "chat")
        object : SwipeHelper(requireContext(), binding.chatsListRecycler) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>,
                position:Int
            ) {

                /*underlayButtons.add(UnderlayButton(
                    requireContext(),
                    "",
                    R.drawable.video_call_image,
                    Color.parseColor("#FFFFFFFF")
                ) {
                    manualUserDataClass.userName = chatList?.result?.get(position)?.name
                    manualUserDataClass.image = chatList?.result?.get(position)?.profile
                    onInvalidData()
                    generateAgoraToken(position)
                })*/
                underlayButtons.add(UnderlayButton(
                    requireContext(),
                    "",
                    R.drawable.clear_chat_image,
                    Color.parseColor("#FFFFFFFF")
                ) {
                    openClearChatDialog(position)
                })
            }
        }
    }
/*
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
*/
/*
    private fun generateAgoraToken(position: Int) {
        Log.i("TAG", "generateAgoraToken:11 ")
        if (::socket.isInitialized){
            Log.i("TAG", "generateAgoraToken:22 ")
            val data = JSONObject()
            try {
                data.put("receiverId", chatList?.result?.get(position)?._id)
                data.put("roomId", chatList?.result?.get(position)?.roomId)
                data.put("senderId", Preferences.getStringPreference(context, USER_ID))
//                data.put("messageType", callType)
//                data.put("message", "")
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
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    findNavController().navigate(
                                        ChatFragmentDirections.chatToOutgoingVideo(
                                            Gson().toJson(videoCallResponse), getString(
                                                R.string.outgoing_video
                                            ), Gson().toJson(manualUserDataClass)
                                        )
                                    )
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
*/

    private fun setObserver() {
        storyViewModel.getUserListingLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            uploadedStory = it.data?.data?.userData?.isStoryExists!!
                            Preferences.setStringPreference(
                                requireContext(),
                                PROFILE,
                                it.data?.data?.userData?.profile
                            )
                            storyListingResponse = it.data
                            myStoryImagePath = it.data.data.userData.profile.toString()
                            adapterStories = AdapterStoriesRecycler(
                                this,
                                uploadedStory,
                                myStoryImagePath,
                                it.data.data.listing,
                                "chat"
                            )
                            binding.storiesRecycler.adapter = adapterStories
                        }
                        403->{
                            it.data.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            UserPreference.clear()
                            UserPreference.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)!!
                            Preferences.removeAllPreference(requireContext())
                            Preferences.setStringPreference(requireContext(), FCM_TOKEN, UserPreference.deviceToken)
                            Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        else -> {}
                    }
//                    binding.idLoadar.root.visibility = View.GONE

                }

                Status.LOADING -> {
//                    binding.idLoadar.root.visibility = View.VISIBLE

                }

                Status.ERROR -> {
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
//                    binding.idLoadar.root.visibility = View.GONE
                }

            }
        }
        storyViewModel.getOtherStoryListingLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (::storyListingResponse.isInitialized){
                                if (it.data.data.story.size>0){
                                    findNavController().navigate(
                                        ChatFragmentDirections.chatToStoryView(
                                            "2",
                                            it.data.data,
                                            storyListingResponse.data.listing[otherUserSelectedPosition]
                                        )
                                    )
                                }else{
                                    Preferences.getStringPreference(requireActivity(), TOKEN)
                                        ?.let { it1 -> storyViewModel.hitUserListingApi(it1) }
                                    Toast.makeText(requireContext(), getString(R.string.story_doesnt_exist), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        else -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
//                    binding.idLoadar.root.visibility = View.GONE

                }

                Status.LOADING -> {
//                    binding.idLoadar.root.visibility = View.VISIBLE

                }

                Status.ERROR -> {
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
//                    binding.idLoadar.root.visibility = View.GONE
                }

            }
        }

    }
    override fun getSelectedStory(position: Int, userId: String) {
        otherUserSelectedPosition = position - 1  //to get otheruserInfo from userlisting api
        if (position!=0){
            Preferences.getStringPreference(requireActivity(), TOKEN)
                ?.let { storyViewModel.hitOtherStoryListingApi(it, userId) }
        }
    }
    override fun uploadStory() {}
    override fun onCallEnd() {
        if (::socket.isInitialized){
            emitNavigationList()
            onNavigationListSocket()
        }
    }
}