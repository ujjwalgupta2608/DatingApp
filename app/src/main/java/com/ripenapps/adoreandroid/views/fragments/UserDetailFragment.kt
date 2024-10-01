package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.internal.FirebaseDynamicLinksImpl.createDynamicLink
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentUserDetailBinding
import com.ripenapps.adoreandroid.databinding.SideOptionsLayoutBinding
import com.ripenapps.adoreandroid.models.CommonResponseSocket
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media
import com.ripenapps.adoreandroid.models.response_models.carddetails.MediaList
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.view_models.UserDetailViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterGalleryInUserDetail
import com.ripenapps.adoreandroid.views.adapters.AdapterInterestsInUserDetail
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@AndroidEntryPoint
class UserDetailFragment : BaseFragment<FragmentUserDetailBinding>() {
    private lateinit var socket: Socket
    private var blockStatus:String=""
    private var isDislikedFromSideOptions: Boolean=false
    private var profileImageUrl = ""
    private val userDetailViewModel by viewModels<UserDetailViewModel>()
    val homeViewModel by viewModels<HomeViewModel>()
    private var readLessMore = "Read More"
    private var userListType = ""
    private var previousScreen = ""
    private var userId = ""
    private var userDescription: String = ""
    private var descriptionMaxLength = 100
    var localMaxLength = descriptionMaxLength
    private var userMedia: MutableList<Media>? = mutableListOf()
    lateinit var layoutManager: FlexboxLayoutManager
    private var name=""
    private var receiverId=""
    private var roomId=""

    override fun setLayout(): Int {
        return R.layout.fragment_user_detail
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hitUserDetailApi()
        layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.alignItems = AlignItems.CENTER
        binding.interestsRecycler.layoutManager = layoutManager
        (binding.interestsRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        setObserver()
    }

    private fun setObserver() {
        userDetailViewModel.getUserDetailLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (it.data?.data?.user!=null){
                                userMedia = it.data?.data?.user?.media
                                name = it.data?.data?.user?.name.toString()
                                receiverId = it.data?.data?.user?._id.toString()
                                roomId = it.data?.data?.roomId.toString()
                                if (it.data?.data?.blockedUsers?.contains(receiverId) == true){
                                    blockStatus="Block"
                                }else{
                                    blockStatus = "Unblock"
                                }
                                if (it.data?.data?.user?.media?.isNullOrEmpty() == false)
                                    binding.viewAllGallery.isVisible = it.data?.data?.user?.media?.size!! >4
                                binding.name.text = it.data?.data?.user?.name
//                                binding.userInfoText.text = it.data?.data?.user?.name+"'s Info"
                                binding.userInfoText.text = getString(R.string.user_s_info)
                                binding.age.text = it.data?.data?.user?.age.toString()+" Years"
                                if (it.data?.data?.user?.userName.isNullOrEmpty()){
                                    Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                                }else{
                                    binding.userName.text = it.data?.data?.user?.userName
                                }
                                if (it.data?.data?.user?.city.isNullOrEmpty()){
                                    binding.locationLayout.isVisible=false
                                }else{
                                    binding.location.text = it.data?.data?.user?.city
                                }
                                if (it.data?.data?.user?.height.isNullOrEmpty()||it.data?.data?.user?.height=="null"||it.data?.data?.user?.height=="0"){
                                    binding.heightLayout.isVisible=false
                                }else{
                                    binding.height.text = it.data?.data?.user?.height+" cm"
                                }
                                if (it.data?.data?.user?.language.isNullOrEmpty()||it.data?.data?.user?.language=="null"){
                                    binding.languageLayout.isVisible=false
                                }else{
                                    binding.language.text = it.data?.data?.user?.language
                                }
                                userDescription = it.data?.data?.user?.bioDescription.toString()
                                profileImageUrl = it.data?.data?.user?.profile.toString()
                                Glide.with(requireContext()).load(it.data?.data?.user?.profile)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.placeholder_image).into(binding.profileImage)
                                if (userDescription.length > descriptionMaxLength + 1) {
                                    setDescriptionText()
                                } else {
                                    binding.description.text = userDescription
                                }
                                if (!it.data?.data?.user?.topInterests.isNullOrEmpty()&& it.data?.data?.user?.topInterests?.size!! >0){
                                    binding.interestsRecycler.adapter =
                                        AdapterInterestsInUserDetail(it.data?.data?.user?.topInterests)
                                }
                                if (!it.data?.data?.user?.media.isNullOrEmpty()&&it.data?.data?.user?.media?.size!! >0){
                                    binding.galleryMediaRecycler.adapter =
                                        AdapterGalleryInUserDetail(
                                            it.data?.data?.user?.media,
                                            previousScreen,
                                            ::getSelectedPosition
                                        )
                                }
                                if (userListType=="0"){
                                    when(it.data?.data?.like_status){
                                        "myConnection"->{
                                            userListType="1"
                                            setUpMyConnectionUsers()
                                        }
                                        "pending"->{
                                            userListType="2"
                                            setUpPendingUsers()
                                        }
                                        "requests"->{
                                            userListType="3"
                                            setUpRequestUsers()
                                        }
                                        "dislike"->{
                                            userListType="4"
                                            setUpDislikeUsers()
                                        }
                                        ""->{
                                            userListType="3"
                                            setUpRequestUsers()
                                        }
                                        else->{
                                            userListType="4"
                                            setUpDislikeUsers()
//                                            showLikeDislike()
                                        }
                                    }

                                }

                            }else{
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.user_not_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> {
                            Glide.with(requireContext()).load("")
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.placeholder_image).into(binding.profileImage)
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
        homeViewModel.getLikeDislikeLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (isDislikedFromSideOptions){
                                isDislikedFromSideOptions=false
                                findNavController().popBackStack()
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
                }

                Status.LOADING -> {}

                Status.ERROR -> {
                    isDislikedFromSideOptions=false
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

    private fun hitUserDetailApi() {
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let { userDetailViewModel.hitUserDetailApi(it, userId) }
    }

    private fun getSelectedPosition(position: Int) {
        findNavController().navigate(
            UserDetailFragmentDirections.userDetailToShowImageVideo(
                MediaList(userMedia), "singleMedia", position.toString(), previousScreen
            )
        )
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
        userListType = arguments?.getString("userListType").toString()
        previousScreen = arguments?.getString("previousScreen").toString()
        userId = arguments?.getString("userId").toString()
        if (isAdded) {
            initializeSocket()
        }
        onBlockUserSuccess()
        onUnblockUserSuccess()
        onUnblockError()
        setupUi()
    }

    private fun initializeSocket() {
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
    }


    private fun setupUi() {
        when (userListType) {
            "0" -> {
//                setUpHideAll()
            }

            "1" -> {
                setUpMyConnectionUsers()
            }

            "2" -> {
                setUpPendingUsers()
            }

            "3" -> {
                setUpRequestUsers()
            }

            "4" -> {
                setUpDislikeUsers()

            }
            "5"->{  //nothing
                setUpHideAll()

            }
            "6" -> {
                showLikeDislike()
            }
        }
    }

    private fun showLikeDislike() {
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.like_dislike_buttons)
        binding.likeButton.isVisible = true
        binding.dislikeButton.isVisible = true
        binding.cancelPendingRequestButton.isVisible = false
        binding.shareImage.isVisible = false
        binding.sideOptions.isVisible = true
    }

    private fun setUpHideAll() {
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.like_icon_with_border)
        binding.bottomButtonLayout.isVisible=false
        binding.likeButton.isVisible = false
        binding.dislikeButton.isVisible = false
        binding.cancelPendingRequestButton.isVisible = false
        binding.shareImage.isVisible = false
        binding.sideOptions.isVisible = false
    }

    private fun setUpDislikeUsers() {
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.like_icon_with_border)
        binding.likeButton.isVisible = false
        binding.dislikeButton.isVisible = false
        binding.cancelPendingRequestButton.isVisible = true
        binding.shareImage.isVisible = true
        binding.sideOptions.isVisible = false
    }

    private fun setUpRequestUsers() {
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.sent_request_buttons)
        binding.likeButton.isVisible = true
        binding.dislikeButton.isVisible = true
        binding.cancelPendingRequestButton.isVisible = false
        binding.shareImage.isVisible = true
        binding.sideOptions.isVisible = false
    }

    private fun setUpAllUsers(){
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.like_icon_with_border)
        binding.likeButton.isVisible = false
        binding.dislikeButton.isVisible = false
        binding.cancelPendingRequestButton.isVisible = true
        binding.shareImage.isVisible = true
        binding.sideOptions.isVisible = false

        /*binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.like_dislike_buttons)
        binding.likeButton.isVisible = true
        binding.dislikeButton.isVisible = true
        binding.cancelPendingRequestButton.isVisible = false
        binding.shareImage.isVisible = false
        binding.sideOptions.isVisible = true*/
    }
    private fun setUpMyConnectionUsers() {
        binding.chatImage.isVisible=true
//                binding.bottomButtonLayout.background =
//                    resources.getDrawable(R.drawable.like_dislike_buttons)
        binding.bottomButtonLayout.isVisible=false
        binding.likeButton.isVisible = false
        binding.dislikeButton.isVisible = false
        binding.cancelPendingRequestButton.isVisible = false
        binding.shareImage.isVisible = false
        binding.sideOptions.isVisible = true
    }
    private fun setUpPendingUsers(){
        binding.chatImage.isVisible=false
        binding.bottomButtonLayout.background =
            resources.getDrawable(R.drawable.cancel_button_red)
        binding.likeButton.isVisible = false
        binding.dislikeButton.isVisible = false
        binding.cancelPendingRequestButton.isVisible = true
        binding.shareImage.isVisible = true
        binding.sideOptions.isVisible = false
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
        popWindow.showAtLocation(view, Gravity.TOP, 250, 300)
        if (blockStatus=="Block"){
            sideOptionsLayoutBinding.block.text=getString(R.string.unblock)
        }else{
            sideOptionsLayoutBinding.block.text=getString(R.string.block)
        }
        sideOptionsLayoutBinding.shareProfile.setOnClickListener {
            popWindow.dismiss()
            createFirebaseLink()
        }
        sideOptionsLayoutBinding.disLike.setOnClickListener {
            popWindow.dismiss()
            openDislikeDialog()
            isDislikedFromSideOptions=true
        }
        sideOptionsLayoutBinding.block.setOnClickListener {
            popWindow.dismiss()
            if (blockStatus=="Block"){
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
                    "${getString(R.string.unblock)} $name",
                    getString(R.string.are_you_sure_you_want_to_unblock_this_user),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )
            }else{
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
                    "${getString(R.string.block)} $name",
                    getString(R.string.are_you_sure_you_want_to_block_this_user),
                    getString(R.string.yes),
                    getString(R.string.no),
                    2
                )
            }
        }
        return true
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
                        var commonResponseSocket= Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            CommonResponseSocket::class.java)
                        if (commonResponseSocket.status==200){
                            Toast.makeText(requireContext(), commonResponseSocket.message, Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
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
                        var commonResponseSocket= Gson().fromJson(
                            JSONArray().put(data1)[0].toString(),
                            CommonResponseSocket::class.java)
                        if (commonResponseSocket.status==200){
                            Toast.makeText(requireContext(), commonResponseSocket.message, Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    } catch (ex: JSONException) {

                    }
                }
            }
        })
    }
    private fun setDescriptionText() {
        val ss = SpannableString("${userDescription.substring(0, localMaxLength)} $readLessMore")
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (readLessMore == "Read More") {
                    localMaxLength = userDescription.length
                    readLessMore = "Read Less"
                    setDescriptionText()
                } else {
                    localMaxLength = descriptionMaxLength
                    readLessMore = "Read More"
                    setDescriptionText()
                }
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = resources.getColor(R.color.theme)
                ds.isUnderlineText = true
                ds.underlineColor = resources.getColor(R.color.theme)
            }
        }
        ss.setSpan(
            clickableSpan1,
            localMaxLength + 1,
            "${userDescription.substring(0, localMaxLength)} $readLessMore".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.description.text = ss
        binding.description.movementMethod = LinkMovementMethod.getInstance()
    }
    private fun shareDynamicLink(dynamicLink: Uri) {
        Log.i("TAG", "shareDynamicLink: $dynamicLink")
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, dynamicLink.toString())
        startActivity(Intent.createChooser(shareIntent, "Share Deep Link"))
    }
    private fun onClick() {
        binding.shareImage.setOnClickListener {
            createFirebaseLink()
        }
        binding.chatImage.setOnClickListener {
            findNavController().navigate(UserDetailFragmentDirections.userDetailToSpecificChat("userDetail", roomId.trim(), receiverId, name, profileImageUrl))
        }
        binding.backButton.setOnClickListener {
            if (previousScreen=="search"){
                requireActivity().finish()
            }else{
                findNavController().popBackStack()
            }
        }
        binding.sideOptions.setOnClickListener {
            popUp(binding.sideOptions)
        }
        binding.viewAllGallery.setOnClickListener {
//            if (previousScreen == "home") {
                findNavController().navigate(
                    UserDetailFragmentDirections.userDetailToShowImageVideo(
                        MediaList(userMedia), "viewAll", "", previousScreen
                    )
                )
//            }
        }
        binding.viewOverUserImage.setOnClickListener {
//            if (previousScreen == "home") {
                findNavController().navigate(
                    UserDetailFragmentDirections.userDetailToShowImageVideo(
                        MediaList(), "profileImage", profileImageUrl, previousScreen
                    )
                )
//            }
        }

        binding.likeButton.setOnClickListener {
            if (previousScreen=="home"){
                UserPreference.isUserDisliked = "like"
                requireActivity().onBackPressed()
            }else{
                openLikeDialog()
            }

        }
        binding.dislikeButton.setOnClickListener {
            if (previousScreen=="home"){
                UserPreference.isUserDisliked = "dislike"
                requireActivity().onBackPressed()
            } else{
                openDislikeDialog()
            }
        }
        binding.cancelPendingRequestButton.setOnClickListener { // work as like button in case of dislike screen
            if (userListType=="2"||userListType=="0"){//pending screen
                openDislikeDialog()
            } else if (userListType=="4"||userListType=="0"){//dislike button
                openLikeDialog()
            }
        }
    }

    private fun createFirebaseLink() {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://adoredatingapp.com").buildUpon()
                .appendQueryParameter("profileId", userId)
                .build())
            .setDomainUriPrefix("https://adoredatingapp.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.ripenapps.adoreandroid")
                    .setMinimumVersion(0)
                    .build()
            ).setIosParameters( DynamicLink.IosParameters.Builder("com.ripenapps.adoreios").setAppStoreId("6497716227").build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(name)
                    .setDescription("${Preferences.getStringPreference(requireContext(), NAME)} sends you ${name}'s profile from Adore.")
                    .setImageUrl(Uri.parse(profileImageUrl))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Short link generated
                    val shortLink: Uri = task.result.shortLink!!
                    // Share the short link using the sharing mechanism of your choice
                    shareDynamicLink(shortLink)
                } else {
                    // Error handling
                }
            }
    }

    private fun openDislikeDialog() {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    Preferences.getStringPreference(requireContext(), TOKEN)
                        ?.let {
                            homeViewModel.hitLikeDislikeUserApi(
                                it,
                                LikeDislikeRequest("dislike"),
                                userId
                            )
                        }
                    binding.bottomButtonLayout.isVisible = false
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                    if (isDislikedFromSideOptions){
                        isDislikedFromSideOptions=false
                    }
                }
            },
            requireContext(),
            getString(R.string.dislike),
            getString(R.string.are_you_sure_you_want_to_dislike_the_user),
            getString(R.string.yes),
            getString(R.string.no)
        )
    }

    private fun openLikeDialog() {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    Preferences.getStringPreference(requireContext(), TOKEN)
                        ?.let {
                            homeViewModel.hitLikeDislikeUserApi(
                                it,
                                LikeDislikeRequest("like"),
                                userId
                            )
                        }
                    binding.bottomButtonLayout.isVisible = false
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.like),
            getString(R.string.are_you_sure_you_want_to_like_this_user),
            getString(R.string.yes),
            getString(R.string.no),
        )
    }
}