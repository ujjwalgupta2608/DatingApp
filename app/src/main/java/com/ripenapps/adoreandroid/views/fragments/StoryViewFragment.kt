package com.ripenapps.adoreandroid.views.fragments

import android.content.Context
import androidx.navigation.NavOptions
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentStoryViewBinding
import com.ripenapps.adoreandroid.models.response_models.InitiateChatResponse
import com.ripenapps.adoreandroid.models.response_models.myStoryList.StoryData
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListing
import com.ripenapps.adoreandroid.models.response_models.storyViewers.StoryViewersResponse
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.PROFILE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.view_models.StoryViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterStory
import com.ripenapps.adoreandroid.views.adapters.AdaptorStoryViews
import com.ripenapps.adoreandroid.views.adapters.VideoCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class StoryViewFragment : BaseFragment<FragmentStoryViewBinding>(), VideoCallback {
    private val viewModel by viewModels<StoryViewModel>()
    private val homeViewModel by viewModels<HomeViewModel>()
    var userStoryData = "" // it's a check 1 for myStory and 2 for otherStory
    var roomId=""
    private lateinit var adapterStory: AdapterStory
    var height: Int = 0
    var width: Int = 0
    var numberOfSections: Int = 0
    var viewsList: ArrayList<View> = ArrayList()
    var activeProgress: SeekBar? = null
    var activePrevProgress: SeekBar? = null
    var activeIndex = 0
    lateinit var storyData: StoryData
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var storyViewersList: StoryViewersResponse
    private lateinit var storyListing: StoryListing

    private lateinit var socket: Socket

    override fun setLayout(): Int {
        return R.layout.fragment_story_view
    }

    override fun initView(savedInstanceState: Bundle?) {
        userStoryData = arguments?.getString("userStoryData").toString()
        if (userStoryData == "1") {
            binding.userName.text = Preferences.getStringPreference(requireActivity(), NAME)
            Glide.with(requireActivity())
                .load(Preferences.getStringPreference(requireActivity(), PROFILE))
                .into(binding.profileImageView)
            storyData = StoryViewFragmentArgs.fromBundle(requireArguments()).myStoryList
        } else {
            storyData = StoryViewFragmentArgs.fromBundle(requireArguments()).myStoryList
            storyListing = StoryViewFragmentArgs.fromBundle(requireArguments()).otherUserInfo!!
            roomId = storyData?.roomId!!
            binding.userName.text = storyListing.name
            Glide.with(requireActivity()).load(storyListing.profile)
                .into(binding.profileImageView)
            Log.i("TAG", "storyListing: " + storyListing)
        }

        if (userStoryData == "1") {
//            setting data at 0 index of story viewpager
            binding.views.text = storyData.story[0].viewerCount.toString() + " ${getString(R.string.views)}"
            if (storyData.story[0].viewerCount!! > 0) {
                viewModel.hitStoryViewersListApi(
                    Preferences.getStringPreference(
                        requireActivity(),
                        TOKEN
                    )!!, storyData.story[0]._id!!
                )
            }

        } else {
            viewModel.hitViewStoryApi(
                Preferences.getStringPreference(requireActivity(), TOKEN)!!,
                storyData.story[0]._id!!
            )
        }
        onClick()
//        keyboardListener()
    }

    /*private fun keyboardListener() {
        keyboardVisibilityListener = KeyboardVisibilityListener(requireActivity()) { isVisible ->
            if (isVisible) {
                Log.i("TAG", "keyboardListener: "+"keyboard isVisible")
                // Keyboard is open
                // Add your logic here
            } else {
                Log.i("TAG", "keyboardListener: "+"keyboard  not Visible")
                // Keyboard is closed
                // Add your logic here
            }
        }
    }*/


    private fun onClick() {
        binding.idPrev.setOnClickListener {
            gotoPrevious()
        }
        binding.idNext.setOnClickListener {
            gotoNext()
        }
        binding.idMiddle.setOnClickListener {
            if (storyData.story[binding.storyViewPager.currentItem].type == "video"){
                replayMedia()
            } else{
                adapterStory.resumeStory()
            }
            binding.chatText.setText("")
            CommonUtils.hideKeyBoard(requireActivity())
        }
        binding.views.setOnClickListener {
            openViewersBottomSheet()
        }
        binding.sideOptions.setOnClickListener {
            binding.sideOptions.isClickable = false
            adapterStory.pauseStory()
//            val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
            val view1: View = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottomsheet_add_delete_story, null)
            bottomSheetDialog.setContentView(view1)
            bottomSheetDialog.show()
            val deleteStory = view1.findViewById<View>(R.id.deleteStory) as TextView
            val addStory = view1.findViewById<View>(R.id.addNewStory) as TextView
            deleteStory.setOnClickListener {
                Preferences.getStringPreference(requireActivity(), TOKEN)
                    ?.let { it1 ->
                        viewModel.hitDeleteStoryApi(
                            it1,
                            storyData.story[binding.storyViewPager.currentItem]._id!!
                        )
                    }
                bottomSheetDialog.dismiss()
            }
            addStory.setOnClickListener {
                bottomSheetDialog.dismiss()
                UserPreference.uploadStory = "1"
                requireActivity().onBackPressed()
            }
        }
        binding.userName.setOnClickListener {
            if (userStoryData=="2"){
                val navOptions: NavOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.storyViewFragment, true)
                    .build()
                findNavController().navigate(
                    StoryViewFragmentDirections.storyViewToUserDetail(
                        "5",
                        previousScreen = "story",
                        userId = storyListing?._id.toString()
                    ), navOptions
                )
            }
        }
        binding.chatText.setOnTouchListener(View.OnTouchListener { v, event ->
            onChatTextOpening()
            return@OnTouchListener true

        })

        binding.sendMessage.setOnClickListener {
            if (binding.chatText.text.isNullOrEmpty()){
                Toast.makeText(requireContext(), getString(R.string.please_enter_something), Toast.LENGTH_SHORT)
                    .show()
            }else{
                if (storyData.story[binding.storyViewPager.currentItem].type == "video"){
                    replayMedia()
                } else{
                    adapterStory.resumeStory()
                }
                initializeSocket()
                CommonUtils.hideKeyBoard(requireActivity())
            }
        }

    }

    private fun onChatTextOpening() {
        adapterStory.pauseStory()
        if (binding.chatText.requestFocus()) {
            binding.chatText.requestFocus()
            binding.chatText.isFocusableInTouchMode = true
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.showSoftInput(binding.chatText, InputMethodManager.SHOW_FORCED)
        }
    }

    private fun initializeSocket() {
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
        if (roomId!=""||!roomId.isNullOrEmpty()) {
            sendTextMessage()
        } else{
            emitInitiateChat()
            onGetRoomId()
        }
    }
    private fun emitInitiateChat(){
        val data = JSONObject()
        data.put("message",binding.chatText.text.trim())
        data.put("sender",Preferences.getStringPreference(context, USER_ID))
        data.put("messageType","")
        data.put("receiver",storyListing._id)
        socket.emit("initiateChat", data)
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
                        sendTextMessage()
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun sendTextMessage() {
        val data = JSONObject()
        data.put("message",binding.chatText.text.trim())
        data.put("sender",Preferences.getStringPreference(context, USER_ID))
        data.put("messageType","text")
        data.put("receiver",storyListing._id)
        data.put("roomId",roomId)
        data.put("isStory", true)
        socket.emit("sendMessage", data)
        binding.chatText.setText("")
    }

    private fun openViewersBottomSheet() {
        binding.views.isClickable = false
        adapterStory.pauseStory()
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_story_views, null)
        bottomSheetDialog.setContentView(view1)
        bottomSheetDialog.show()
        val storyRecycler = view1.findViewById<View>(R.id.storyViewsRecycler) as RecyclerView
        val numberOfViews = view1.findViewById<View>(R.id.numberOfViews) as TextView
        if (::storyViewersList.isInitialized && storyViewersList.data.viewersList.isNotEmpty()) {
            numberOfViews.text = storyViewersList.data.viewersList.size.toString() + " ${getString(R.string.views)}"
            storyRecycler.adapter = AdaptorStoryViews(::getSelectedViewer, storyViewersList)
        }
    }

    private fun getSelectedViewer(userId: String) {
        bottomSheetDialog.dismiss()
        val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(R.id.storyViewFragment, true)
            .build()
        findNavController().navigate(
            StoryViewFragmentDirections.storyViewToUserDetail(
                "5",
                previousScreen = "story",
                userId = userId
            ), navOptions
        )
    }

    private fun setListener() {
        bottomSheetDialog.setOnDismissListener {
//            adapterStory.resumeStory()
            if (storyData.story[binding.storyViewPager.currentItem].type == "video"){
                replayMedia()
            } else{
                adapterStory.resumeStory()
            }
            binding.views.isClickable = true
            binding.sideOptions.isClickable = true
        }
    }

    override fun onPause() {
        super.onPause()
        CommonUtils.hideKeyBoard(requireActivity())
        if(storyData!=null&&storyData.story!=null&&storyData.story.size>binding.storyViewPager.currentItem){
            if (storyData.story[binding.storyViewPager.currentItem].type == "video") {
                adapterStory.seekTo = null
                adapterStory.isVideoPlaying = false
                adapterStory.storyJob?.cancel()
                adapterStory.clearPreviousJob()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)

        if (userStoryData == "1") {
            binding.chatLayout.isVisible = false
            binding.sideOptions.isVisible = true
            binding.views.isVisible = true
        } else {
            binding.chatLayout.isVisible = true
            binding.sideOptions.isVisible = false
            binding.views.isVisible = false
        }
//        set all data of selected user
        setListener()
        setDynamicSeekbar()
        setObserver()
    }

    private fun setObserver() {
        viewModel.getStoryViewersLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            storyViewersList = it.data

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
                }

                Status.LOADING -> {}

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.data?.data}")
                }

            }
        }
        viewModel.getDeleteStoryLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            storyData.story.removeAt(binding.storyViewPager.currentItem)
                            adapterStory.clearPreviousJob()
//                            adapterStory.updateStories()
                            requireActivity().onBackPressed()
//                            setDynamicSeekbar()
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
        viewModel.getViewStoryLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            Log.i("TAG", "viewStory: " + it.data?.message)
                        }

                        else -> {

                            Log.i("TAG", "viewStory: " + it.data?.message)

                        }
                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {}

            }
        }
    }

    private fun setDynamicSeekbar() {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
        width =
            displayMetrics.widthPixels - resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp)
        numberOfSections = storyData.story.size
        width -= 10 * numberOfSections //reducing length of layout by 16dp from left and right and in between 16dp {in between*number of sections)
        width /= numberOfSections
        binding.seekbarLayout.removeAllViews()

        for (i in 0 until numberOfSections) {
            val v = SeekBar(requireActivity(), null, android.R.attr.progressBarStyleHorizontal)
            val params = LinearLayout.LayoutParams(width, 8)
            params.setMargins(5, 0, 5, 0) //giving 16dp internal margin between two views
            v.layoutParams = params
            v.progressDrawable = resources.getDrawable(R.drawable.progress_drawable)
            v.max = 100
            viewsList.add(v)
            v.setBackgroundColor(resources.getColor(R.color.white_20))
            binding.seekbarLayout.addView(v)
        }
        binding.storyViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapterStory.notifyDataSetChanged()
                activateIndex(position)
                adapterStory.initUi(position)
                adapterStory.resumeStory()
                if (userStoryData == "1") {
                    if (::storyViewersList.isInitialized) {
                        storyViewersList.data.viewersList.clear()
                    }
                    binding.views.text = storyData.story[position].viewerCount.toString() + " ${getString(R.string.views)}"
                    if (storyData.story[position].viewerCount!! > 0) {
                        viewModel.hitStoryViewersListApi(
                            Preferences.getStringPreference(
                                requireActivity(),
                                TOKEN
                            )!!, storyData.story[position]._id!!
                        )
                    }
                } else {
                    viewModel.hitViewStoryApi(
                        Preferences.getStringPreference(
                            requireActivity(),
                            TOKEN
                        )!!, storyData.story[position]._id!!
                    )
                }
            }
        })

        setItemRecycler()
    }


    private fun activateIndex(position: Int) {
        (0 until viewsList.size).forEach {
            when {
                it < position -> {
                    activePrevProgress = viewsList[it] as SeekBar
                    activePrevProgress!!.progress = Integer.MAX_VALUE
                }

                it == position -> {
                    activeProgress = viewsList[position] as SeekBar
                }

                else -> {
                    activePrevProgress = viewsList[it] as SeekBar
                    activePrevProgress!!.progress = 0
                }
            }
            activeIndex = position
        }
    }

    private fun setItemRecycler() {
        adapterStory = AdapterStory(
            requireActivity(),
            this,
            storyData,
            lifecycleScope
        )
        binding.storyViewPager.setOnTouchListener { arg0, arg1 -> true } //stop swiping on swipe left or right
        binding.storyViewPager.adapter = adapterStory
        binding.storyViewPager.isUserInputEnabled = false;
//        binding.storyViewPager.offscreenPageLimit = 1
        if (viewsList.size > 0)
            activeProgress = viewsList[0] as SeekBar

    }

    private fun gotoNext() {
        adapterStory.seekTo = null
        adapterStory.isVideoPlaying = false
        adapterStory.storyJob?.cancel()
        adapterStory.clearPreviousJob()
        if (binding.storyViewPager.currentItem == adapterStory.itemCount - 1) {
            if (isAdded)
                requireActivity().onBackPressed()
        } else {
            binding.storyViewPager.setCurrentItem(binding.storyViewPager.currentItem + 1, false)
        }
    }

    private fun gotoPrevious() {
        if (binding.storyViewPager.currentItem > 0) {
            adapterStory.seekTo = null
            adapterStory.isVideoPlaying = false
            adapterStory.storyJob?.cancel()
            adapterStory.clearPreviousJob()
            binding.storyViewPager.setCurrentItem(binding.storyViewPager.currentItem - 1, false)
        }
    }
    private fun replayMedia() {
        if (binding.storyViewPager.currentItem > 0) {
            adapterStory.seekTo = null
            adapterStory.isVideoPlaying = false
            adapterStory.storyJob?.cancel()
            adapterStory.clearPreviousJob()
            binding.storyViewPager.setCurrentItem(binding.storyViewPager.currentItem, false)
            adapterStory.notifyDataSetChanged()
        }
    }

    override fun initVideo() {
//        Log.i("TAG", "loading: " + " initvideo")
        activeProgress?.progress = 0
    }

    override fun onStoryLoad(duration: Int) {
//        Log.i("TAG", "loading: " + " onstoryload")
        activeProgress?.progress = 0
        activeProgress?.max = duration
    }

    override fun loading(fragPos: Int, progress: Int) {
//        Log.i("TAG", "loading: "+" loading")
        if (fragPos == activeIndex)
            activeProgress?.progress = progress
    }

    override fun onStoryCompleted(progress: Int) {
//        Log.i("TAG", "loading: " + " onstorycomp")
        activeProgress?.progress = progress
        gotoNext()
    }

    override fun actionDownEvent() {
        requireActivity().onBackPressed()
    }

    override fun hideViews() {
        binding.headerLayout.isVisible = false
        if (userStoryData == "1") {
            binding.views.isVisible = false
        } else {
            binding.chatLayout.isVisible = false
        }
    }

    override fun showViews() {
        binding.headerLayout.isVisible = true
        if (userStoryData == "1") {
            binding.views.isVisible = true
        } else {
            binding.chatLayout.isVisible = true
        }
    }

    override fun openViewersList() {
        if (userStoryData == "1") {
            openViewersBottomSheet()
        } else{
            onChatTextOpening()
        }
    }
}

