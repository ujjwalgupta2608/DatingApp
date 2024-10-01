package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.android.billingclient.api.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.DialogSubscriptionBinding
import com.ripenapps.adoreandroid.databinding.FragmentHomeBinding
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.models.request_models.PlanRenewModel
import com.ripenapps.adoreandroid.models.response_models.cardlist.CardListUser
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListing
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListingResponse
import com.ripenapps.adoreandroid.models.static_models.NotificationMessageData
import com.ripenapps.adoreandroid.preferences.*
import com.ripenapps.adoreandroid.stackview.*
import com.ripenapps.adoreandroid.utils.*
import com.ripenapps.adoreandroid.view_models.HomeViewModel
import com.ripenapps.adoreandroid.view_models.MyProfileViewModel
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import com.ripenapps.adoreandroid.view_models.StoryViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterStoriesRecycler
import com.ripenapps.adoreandroid.views.adapters.CardStackAdapter
import com.yuyakaido.android.cardstackview.internal.CallBackInterface
import dagger.hilt.android.AndroidEntryPoint
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), AdapterStoriesRecycler.StorySelector,
    CardStackListener, CallBackInterface {
    private var premiumPlanPrice=""
    private var purchaseToken="0"
    private var planExpireDate=""
    private var filterOrBoostPressed = false    //get_plan api used to get price when filterOrBoostPressed are pressed and to get purchase key detail for checking subscription when app opens
    private var isSubscriptionActive = false
    private val PAGINATION_LIMIT: Int = 10
    private var otherUserSelectedPosition: Int = -1
    private var uploadedStory = false
    private val homeViewModel by viewModels<HomeViewModel>()
    private val storyViewModel by viewModels<StoryViewModel>()
    private val profileViewModel by viewModels<MyProfileViewModel>()
    private var cameraGalleryCheck = 0   // 1 for camera, 2 for gallery
    private val VideoRequestCode = 122
    private var path = ""
    private var capturedImageUri: Uri? = null
    private var galleryImagePathList = arrayListOf<String>()
    private var galleryVideoPathList = arrayListOf<String>()
    private var myStoryMediaPathList = arrayListOf<String>()
    private var saveAt = ""
    private var vdo_path = ""
    private lateinit var adapterStories: AdapterStoriesRecycler
    private var myStoryImagePath = ""
    private lateinit var storyListingResponse: StoryListingResponse

    private lateinit var manager: CardStackLayoutManager
    private var adapter: CardStackAdapter? = null
    private lateinit var direct: Direction
    private var cardButtonsChk = 1
    private var adapterPosition: Int = -1
    private var loading = false
    private var currentPage = 1
    var cardListUser: MutableList<CardListUser> = mutableListOf()
    private var cardHandler = Handler()
    private var job: Job? = null
    private val planViewModel by viewModels<PlanViewModel>()
    private var imagerequestcode: Int = 0
    private var imagePath: String = ""
    private lateinit var billingClient: BillingClient
    private var isUserNavigatedBeforeSubsCheck = 0


    override fun setLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = homeViewModel
        Preferences.getStringPreference(requireActivity(), TOKEN)
            ?.let { storyViewModel.hitUserListingApi(it) }
        cardListObserver()
        setPagination()
        likeDislikeUserObserver()
        setObserver()
    }

    private fun goToLocation() {
        if (UserPreference.isNewUserRegistered) {   //coming from login or signup
            if (CommonUtils.checkPermissions(requireContext())){
                isUserNavigatedBeforeSubsCheck=1
                findNavController().navigate(HomeFragmentDirections.homeToSearchLocation())
            }else{
                UserPreference.isNewUserRegistered = false
                isUserNavigatedBeforeSubsCheck=1
                findNavController().navigate(HomeFragmentDirections.homeToLocation())
            }
        }else if (Preferences.getStringPreference(requireContext(), CITY).isNullOrEmpty()){ //when user close app without saving location after signup
            UserPreference.isNewUserRegistered=true
            isUserNavigatedBeforeSubsCheck=1
            findNavController().navigate(HomeFragmentDirections.homeToSearchLocation())
        }else{
            if (isAdded){
                Log.i("TAG", "cardListObserver: "+UserPreference.NotificationData)
                if (UserPreference.NotificationData.notificationType=="chat"){
                    isUserNavigatedBeforeSubsCheck=1
                    findNavController().navigate(HomeFragmentDirections.homeToSpecificChat("home",
                        UserPreference.NotificationData.roomId!!,
                        UserPreference.NotificationData.userId!!,
                        UserPreference.NotificationData.userName!!,
                        UserPreference.NotificationData.profile!!
                    ))
                    UserPreference.NotificationData= NotificationMessageData()
                }else if (UserPreference.NotificationData.notificationType=="card" && Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                    isUserNavigatedBeforeSubsCheck=1
                    findNavController().navigate(
                        HomeFragmentDirections.homeToUserDetail(
                            userListType = "0",
                            previousScreen = "like",
                            userId = UserPreference.NotificationData.userId!!
                        )
                    )
                    UserPreference.NotificationData= NotificationMessageData()
                }else if (UserPreference.deepLinkProfileId.isNotEmpty()){
                    isUserNavigatedBeforeSubsCheck=1
                    findNavController().navigate(
                        HomeFragmentDirections.homeToUserDetail(
                            userListType = "0",
                            previousScreen = "like",
                            userId = UserPreference.deepLinkProfileId
                        )
                    )
                    UserPreference.deepLinkProfileId=""
                }else if(isUserNavigatedBeforeSubsCheck==0){
                    if (!filterOrBoostPressed) {
                        hitPlanListApi()
                    }
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Main) {
                ProcessDialog.showDialog(requireActivity(), true)
            }
            UserPreference.savedLanguageCode = Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE).toString()
            initBillingClient()
//            Preferences.getStringPreference(requireContext(), TOKEN)
//                ?.let { homeViewModel.hitCardListApi(it, currentPage, 10, UserPreference.filterCardListRequestKeys) }
        }
        binding.storiesRecycler.adapter = AdapterStoriesRecycler(this, false, "", listOf(), "home")
        initCardAdapter()
        onClick()
        Log.i("TAG", "initView: token"+Preferences.getStringPreference(requireContext(), TOKEN))
    }

    override fun onResume() {
        super.onResume()

        if (UserPreference.uploadStory == "1") {
            UserPreference.uploadStory = ""
            askPermission()
        }
        if (UserPreference.isFilterApplied){
            currentPage=1
            UserPreference.isFilterApplied=false
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let { homeViewModel.hitCardListApi(it, currentPage, 10, UserPreference.filterCardListRequestKeys) }
        }
        if (isUserNavigatedBeforeSubsCheck==1){
            isUserNavigatedBeforeSubsCheck=0
            hitPlanListApi()
        }
        setLocation()
        checkBoost()
        if (UserPreference.isUserDisliked == "dislike") {
            UserPreference.isUserDisliked = ""
            swipeLeft()
        } else if (UserPreference.isUserDisliked == "like") {
            UserPreference.isUserDisliked = ""
            swipeRight()
        }
    }
    private fun initBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener { _, _ -> }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    getSubscriptionPrice()
                    querySubscriptionsHistory()
//                    queryPurchaseSubscriptionsHistory()
                    Log.i("TAG", "onBillingSetupFinished: if")
                } else {
                    Log.i("TAG", "onBillingSetupFinished: else")
                    // Handle setup failure
                }
            }

            override fun onBillingServiceDisconnected() {

                // Handle disconnected
            }
        })
    }

    private fun getSubscriptionPrice() {
        val productList = QueryProductDetailsParams.Product.newBuilder()
            .setProductId("adore_product1234")
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params1 = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(productList))
            .build()
        billingClient.queryProductDetailsAsync(params1) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                // Handle the retrieved product details
                for (productDetails in productDetailsList) {
                    // Get pricing information
                    premiumPlanPrice = productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice.toString()
                    Preferences.setStringPreference(requireContext(), PLAN_AMOUNT, premiumPlanPrice)
                    // Use the price and currency code as needed
                    Log.i("Subscription productdetails", Gson().toJson(productDetails))
                }
            } else {
                // Handle any errors in querying product details
            }
        }
    }

    fun queryPurchaseSubscriptionsHistory(){
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchaseHistoryAsync(params, object : PurchaseHistoryResponseListener {
            override fun onPurchaseHistoryResponse(billingResult: BillingResult, purchaseHistoryList: List<PurchaseHistoryRecord>?) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseHistoryList != null) {
                    for (purchase in purchaseHistoryList) {
                        val purchaseData = purchase.originalJson
                        val purchaseJson = JSONObject(purchaseData)
                        val expiryTime = purchaseJson.optLong("expiryTimeMillis")
                        Log.i("TAG", "billingResult: "+Gson().toJson(billingResult))
                        Log.i("TAG", "purchaseHistoryList: "+Gson().toJson(purchaseHistoryList))

                        // Do something with the expiryTime
                    }
                } else {
                    // Handle error or empty purchase history
                }
            }
        })
    }
    fun querySubscriptionsHistory() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult: BillingResult, purchasesList: List<Purchase>? ->
                Log.i("TAG", "querySubscriptions: "+Gson().toJson(billingResult))
                Log.i("TAG", "querySubscriptions: "+Gson().toJson(purchasesList))
                if (purchasesList.isNullOrEmpty() || billingResult.responseCode != BillingClient.BillingResponseCode.OK){
//                Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "0")
                    Log.i("TAG", "querySubscriptionsHistory: "+"1")
                    isSubscriptionActive = false
                    Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, "")
//                ProcessDialog.dismissDialog(true)
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList != null) {
                    Log.i("TAG", "querySubscriptionsHistory: "+"2")
                    for (purchase in purchasesList) {
                        Log.i("TAG", "querySubscriptionsHistory: "+"3")

                        // Handle the purchased subscription
                        // Calculate subscription start and end times based on purchase time and subscription period
                        // Assuming subscription period is in milliseconds (e.g., 30 days)
                        // 30 * 24 * 60 * 60 * 1000L // 30 days in milliseconds  -->  replace this with 5 min after testing mode changed to live
                        val subscriptionPeriodMillis = /*30 * 24 * 60*/ 5 * 60 * 1000L // 5 min in milliseconds
//                        var subscriptionEndTimeMillis = purchase.purchaseTime + subscriptionPeriodMillis
                        // Convert start time and end time to date with a specific time format
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        Log.i("TAG", "querySubscriptionsHistory: starttime "+dateFormat.format(Date(purchase.purchaseTime)))
                        val calendar = Calendar.getInstance()
                        calendar.time = Date(purchase.purchaseTime)
                        calendar.add(Calendar.MONTH, 1)    // replace with 1 month calendar.add(Calendar.MONTH, 1)
                        var subscriptionEndTime = dateFormat.format(calendar.time)
                        Log.i("TAG", "subscriptionEndTime:first $subscriptionEndTime")
                        var maxCheck=0
                        while (CommonUtils.compareDates( CommonUtils.getFormattedDateToday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), subscriptionEndTime,"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                            Log.i("TAG", "querySubscriptionsHistory: "+"4")
                            calendar.add(Calendar.MINUTE, 5)
//                            subscriptionEndTimeMillis += subscriptionPeriodMillis
                            subscriptionEndTime = dateFormat.format(calendar.time)
                            maxCheck++
                            if (maxCheck==12)
                                break
                        }
                        Log.i("TAG", "subscriptionEndTime: $subscriptionEndTime")
                        if (purchase.isAutoRenewing){
                            planExpireDate=subscriptionEndTime
                            isSubscriptionActive=true
                            purchaseToken = purchase.purchaseToken
                        }else{
                            planExpireDate=subscriptionEndTime
                            isSubscriptionActive=false
                            purchaseToken = purchase.purchaseToken
                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, "")
                        }
//                    Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, it.data.data?.boostData?.subscriptionExpireDate)
                        // You can use subscriptionId to identify the purchased subscription
                    }
//                ProcessDialog.dismissDialog(true)
                }else{
//                ProcessDialog.dismissDialog(true)
                }
                Preferences.getStringPreference(requireContext(), TOKEN)
                    ?.let { homeViewModel.hitCardListApi(it, currentPage, 10, UserPreference.filterCardListRequestKeys) }
            }
    }
    private fun askPermission() {
        val permission: Array<String?> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            )
            else arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (CommonUtils.requestPermissionsNew(requireActivity(), 100, permission)) {
            imagerequestcode = 1
            openCameraGalleryBottomSheet()
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
    }
    private fun checkBoost() {
        job = CoroutineScope(Dispatchers.Main).launch {
            repeat(140) { // Repeat every 15 seconds
//                Log.i("TAG", "compareDates: "+CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
//                Log.i("TAG", "currentTime: "+CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
//                Log.i("TAG", "endtime: "+UserPreference.endTime!!)
                if (!UserPreference.endTime.isNullOrEmpty()&&Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                    if (CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                        Log.i("TAG", "isBoosted: "+ CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                        binding.boostTimer.text = (CommonUtils.subractTimes(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")+1).toString()

                    }else{
                        binding.boostTimer.text = UserPreference.boostCount.toString()
                    }
                }else if(UserPreference.endTime.isNullOrEmpty() && Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                    binding.boostTimer.text = UserPreference.boostCount.toString()
                }
                else{
                    binding.boostTimer.isVisible = false
                }
                delay(10000) // 15 seconds delay
            }
        }
    }

    private fun setLocation() {
        binding.location.text = Preferences.getStringPreference(requireContext(), CITY)+", "+Preferences.getStringPreference(requireContext(), COUNTRY)
    }
    private fun setObserver() {
        planViewModel.getPlanRenewLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data.status) {
                            200 -> {
                                UserPreference.boostCount=5
                                setBoost()
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
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
        planViewModel.getPlanListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                if (filterOrBoostPressed){
                                    filterOrBoostPressed=false
                                    createSubscriptionDialog(it.data.data?.plan?.get(1)?.discountPrice.toString())

                                }else{
                                    Log.i("TAG", "setObserver: getCurrentime"+CommonUtils.getFormattedDateToday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
//                                    user never subscribed before, simply off the subscription and do nothing
                                    if (it.data.data?.purchase==null){  //endTime key has not created in backend database if user has never subscribed before
                                        Log.i("TAG", "setObserver: "+"get null purchase")
                                        UserPreference.hasUserSubscribedBefore="0"
                                        Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "0")
                                        setBoost()
                                    } else{ //user subscribed before
                                        UserPreference.hasUserSubscribedBefore="1"
                                        //uniqueId is matched & account is active
                                        if (it.data.data?.purchase?.inAppPurchasePlanId==purchaseToken && isSubscriptionActive){
                                            Log.i("TAG", "setObserver: "+"case 1")
                                            Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "1")
                                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, planExpireDate)
                                            if (CommonUtils.compareDates(planExpireDate, it.data.data?.purchase?.expiryDate.toString(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                                                planViewModel.hitPlanRenewApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanRenewModel(it.data.data?.plan?.get(1)?._id, purchaseToken, planExpireDate))
                                            }else{
                                                setBoost()
                                            }
                                        }
                                        //there may be a user who subscribed from same google account sometime earlier but currently unsubscribed, or a user subscribed or unsubscribed from another google account, to verify this user his expiry date will be compared
                                        else if (it.data.data?.purchase?.inAppPurchasePlanId!=purchaseToken && CommonUtils.compareDates(it.data.data?.purchase?.expiryDate.toString(), CommonUtils.getFormattedDateToday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                                            Log.i("TAG", "setObserver: "+"case 3")
                                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, it.data.data?.purchase?.expiryDate.toString())
                                            Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "1")
//                                            if (!isSubscriptionActive){
                                                UserPreference.userSubscribedFromAnotherPlayStore="1"
//                                            }else{
//                                                UserPreference.userSubscribedFromAnotherPlayStore="0"
//                                            }
                                            setBoost()
                                        }
                                        //there may be a user who subscribed from same google account sometime earlier but currently unsubscribed, or a user subscribed or unsubscribed from another account
                                        else if (it.data.data?.purchase?.inAppPurchasePlanId!=purchaseToken && !isSubscriptionActive){
                                            Log.i("TAG", "setObserver: "+"case 2")
                                            Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "0")
                                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, "")
                                            setBoost()
//                                            planViewModel.hitPlanRenewApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanPurchaseRequest(it.data.data?.plan?.get(1)?._id, "", ""))
                                        }
                                        // when receipt data is received even after cancelling subs, it continuing the subs for remaining time of current subs
                                        else if (it.data.data?.purchase?.inAppPurchasePlanId==purchaseToken && !isSubscriptionActive && CommonUtils.compareDates(it.data.data?.purchase?.expiryDate.toString(), CommonUtils.getFormattedDateToday("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                                            Log.i("TAG", "setObserver: "+"case 5")
                                            Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "1")
                                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, planExpireDate)
                                            setBoost()
                                        } else{
                                            Log.i("TAG", "setObserver: "+"case else")
                                            Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "0")
                                            Preferences.setStringPreference(requireContext(), PLAN_EXPIRE_DATE, "")
                                            setBoost()
                                        }
                                    }
//                                    checkBoost()
                                    if (it.data.data?.purchase?.inAppPurchasePlanId!=purchaseToken && isSubscriptionActive){
                                        Log.i("TAG", "setObserver: "+"isAnatherAccountSubscribed")
                                        UserPreference.anotherUserSubscribedFromThisPlayStore="1"
                                    }else{
                                        UserPreference.anotherUserSubscribedFromThisPlayStore="0"
                                    }

                                    Log.i("TAG", "getPlanDetail: "+Gson().toJson(it.data?.data))
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
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)
                }

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
                    it.message?.let { it1 ->
                        Toast.makeText(
                            requireActivity(),
                            it1,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
        profileViewModel.getBoostProfileLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            Log.i("TAG", "setObserver: " + it.data)
                            UserPreference.boostCount = it.data?.data?.response?.data?.boostCount
                            UserPreference.endTime=it.data.data?.response?.data?.endTime
                            Toast.makeText(requireContext(), getString(R.string.your_profile_is_boosted_for_next_30_min), Toast.LENGTH_SHORT).show()
                            binding.boostTimer.isVisible = true
                            binding.boostTimer.text = "31"
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

                Status.ERROR -> {}

            }
        }
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
                                "home"
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
        storyViewModel.myStoryListingLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (it.data.data.story.size>0){
                                findNavController().navigate(
                                    HomeFragmentDirections.homeToStoryView(
                                        "1",
                                        it.data.data,
                                        StoryListing()
                                    )
                                )
                            }else{
                                Preferences.getStringPreference(requireActivity(), TOKEN)
                                    ?.let { it1 -> storyViewModel.hitUserListingApi(it1) }
                                Toast.makeText(requireContext(), getString(R.string.story_doesnt_exist), Toast.LENGTH_SHORT).show()
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
        storyViewModel.getOtherStoryListingLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (it.data.data.story.size>0){
                                findNavController().navigate(
                                    HomeFragmentDirections.homeToStoryView(
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
        homeViewModel.getUploadStoryLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it?.data?.status) {
                        200 -> {
                            Toast.makeText(
                                requireActivity(),
                                it.data?.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            Preferences.getStringPreference(requireActivity(), TOKEN)
                                ?.let { it1 -> storyViewModel.hitUserListingApi(it1) }

                        }

                        else -> {
                            Toast.makeText(
                                requireActivity(),
                                it.data?.message,
                                Toast.LENGTH_SHORT
                            ).show()
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

    }

    private fun createSubscriptionDialog(price:String) {
        Log.i("TAG", "createSubscriptionDialog: "+price)
        var dialog= Dialog(requireContext())
        val dialogBinding: DialogSubscriptionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_subscription,
            null,
            false
        )!!
        dialog?.let {
            it.setContentView(dialogBinding.root)
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.show()
        }
        dialogBinding.isFreeSelected=true
        dialogBinding.subscriptionAmount=price
        dialogBinding.crossButton.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.noThanks.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.freeOuterLayout.setOnClickListener {
            if (dialogBinding.isFreeSelected==false){
                dialogBinding.isFreeSelected=true
            }
        }
        dialogBinding.popularOuterLayout.setOnClickListener {
            if (dialogBinding.isFreeSelected==true){
                dialogBinding.isFreeSelected=false
            }
        }
        dialogBinding.subscribeNowButton.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(HomeFragmentDirections.homeToChoosePlan())
        }
    }

    private fun setBoost() {
        if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
            binding.boostTimer.isVisible=true
            if (!UserPreference.endTime.isNullOrEmpty()){
                if (CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                    binding.boostTimer.text = (CommonUtils.subractTimes(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")+1).toString()
                }else{
                    binding.boostTimer.text= UserPreference.boostCount.toString()
                }
            }else{
                binding.boostTimer.text= UserPreference.boostCount.toString()
            }
        }else{
            binding.boostTimer.isVisible=false
        }
    }


    private fun onClick() {
        binding.filter.setOnClickListener {
            filterOrBoostPressed=true
            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                findNavController().navigate(HomeFragmentDirections.homeToFilter())
            } else{
                openSubscriptionDialog()
            }
        }
        binding.notification.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.homeToNotification())
        }
        binding.location.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.homeToSearchLocation())
        }
        binding.boost.setOnClickListener {
            filterOrBoostPressed=true
            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                if (!UserPreference.endTime.isNullOrEmpty()){
                    if (CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                        Log.i("TAG", "isBoosted: "+ CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                        showRemainingBoostPopup()
                    }else{
                        if (UserPreference.boostCount!!>0){
                            openBoostPopup()
                        }else{
                            boostsFinishedPopup()
                            //consumed all boosts
                        }
                    }
                }else{
                    openBoostPopup()
                }
            }else{
                openSubscriptionDialog()
            }
        }
    }
    private fun openBoostPopup(){
        createBoostAvailableDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                    hitBoostApi()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            "${UserPreference.boostCount} ${getString(R.string.boost_available)}",
            getString(R.string.be_the_top_of_your_profile_in_your_area_for_next),
            getString(R.string.boost_me),
            " ${getString(R.string.no_thanks)}",
            2
        )
    }

    private fun hitBoostApi() {
        if (UserPreference.boostCount!!>0){
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let { profileViewModel.hitBoostProfileApi(it) }

        }else{
            Toast.makeText(requireContext(), getString(R.string.no_boost_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun boostsFinishedPopup() {
        createBoostAvailableDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.no_boost_left),
            getString(R.string.you_have_consumed_all_the_boosts_subscribe),
            "",
            "",
            isCancellable = true,
            showButtons = 0
        )
    }
    private fun showRemainingBoostPopup() {
        createBoostAvailableDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            "${UserPreference.boostCount} ${getString(R.string.boost_available)}",
            "0:${(CommonUtils.subractTimes(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")+1)} minutes left!",
            "",
            "",
            isCancellable = true,
            showButtons = 0
        )
    }
    private fun openSubscriptionDialog() {
        createSubscriptionDialog(premiumPlanPrice)
//        Preferences.getStringPreference(requireContext(), TOKEN)
//            ?.let { planViewModel.hitPlanListApi(it) }
    }

    private fun likeDislikeUserObserver() {
        homeViewModel.getLikeDislikeLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            if (it.data?.data?.statusReceiver == "like" && it.data?.data?.statusSender == "like") {
                                cardHandler.removeCallbacksAndMessages(null)
                                cardHandler.postDelayed({
                                    if (isAdded){
                                        if (it.data?.data?.sender?.id!=Preferences.getStringPreference(requireContext(), USER_ID)){
                                            findNavController().navigate(HomeFragmentDirections.homeToLikesMatch("home", it.data?.data?.roomId!!, it.data?.data?.sender?.id!!, it.data?.data?.sender?.name!!, it.data?.data?.sender?.profileUrl!!, it.data?.data?.distance!!))
                                        } else{
                                            findNavController().navigate(HomeFragmentDirections.homeToLikesMatch("home", it.data?.data?.roomId!!, it.data?.data?.receiver?.id!!, it.data?.data?.receiver?.name!!, it.data?.data?.receiver?.profileUrl!!, it.data?.data?.distance!!))
                                        }
                                    }
                                }, 400)
                            }
                            Log.i("TAG", "likeDislikeUserObserver: ")
                        }
                        400->{
                            rewind(adapterPosition)
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
    private fun hitPlanListApi() {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { planViewModel.hitPlanListApi(it) }
    }
    private fun cardListObserver() {
        homeViewModel.getCardListApi().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
//                            if (currentPage > 1)
                            Log.i("TAG", "cardListObserver: "+it.data?.data)
                            adapterPosition = 0
                            loading = true
                            cardListUser.clear()
                            cardListUser = it.data.data?.user!!
                            it.data.data?.let { it1 -> adapter?.updateCardList(it1.user) }
                            if (UserPreference.filterCardListRequestKeys.interestedIn.isNullOrEmpty()) //whenever card filter is applied and api hits it's value was updating, to stop this, this check is used
                                UserPreference.filterCardListRequestKeys.interestedIn = it.data.data?.interestedIn.toString()
                            UserPreference.filterMapListRequestKeys.interestedIn = it.data.data?.interestedIn.toString()
                            if (it.data?.data?.boostData?.endTime?.isNullOrEmpty() == false){
                                UserPreference.endTime=it.data.data?.boostData?.endTime

                            }else{
                                UserPreference.endTime=""
                            }
                            if (it.data?.data?.boostData?.boostCount!=-1){
                                UserPreference.boostCount=it.data.data?.boostData?.boostCount
                            }else{
                                UserPreference.boostCount=5
                            }
                            initStackView()
                            goToLocation()
                            ProcessDialog.dismissDialog(true)
                        }
                        403->{
                            ProcessDialog.dismissDialog(true)
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
                    ProcessDialog.dismissDialog(true)
                }

            }
        }
    }

    private fun setPagination() {
        binding.cardStackView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (adapterPosition == adapter!!.itemCount - 1&& adapterPosition > PAGINATION_LIMIT-1) {
                    if (loading) {
                        currentPage++
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let { homeViewModel.hitCardListApi(it, currentPage, 10, UserPreference.filterCardListRequestKeys) }
                    }
                    loading = false
                }
            }
        })
    }

    private fun initCardAdapter() {
        adapter = CardStackAdapter(
            requireContext(),
            mutableListOf(),
            object : CardStackAdapter.CallBackInterfaces {
                override fun callBack(id: Int, position: Int) {
                    when (id) {
                        0 -> {// dislikeButton
                            swipeLeft()
                        }

                        1 -> {// likeButton
                            swipeRight()
                        }

                        2 -> {
                            findNavController().navigate(
                                HomeFragmentDirections.homeToUserDetail(
                                    userListType = "6",
                                    previousScreen = "home",
                                    userId = cardListUser[position]._id!!
                                )
                            )

                        }
                        3->{
                            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                                rewind(position)
                            } else{
                                filterOrBoostPressed=true
                                openSubscriptionDialog()
                            }
                        }
                        4->{
                            swipeLeft()
                        }
                    }
                }
            })
        initStackView()
    }

    private fun swipeRight() {  //on tap like button
//        UserPreference.numberOfRewind++
        direct = Direction.Right
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        binding.cardStackView.swipe()
    }

    private fun swipeTop() {
        direct = Direction.Top
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        binding.cardStackView.swipe()
    }

    private fun swipeLeft() {   //on tap dislike button
//        UserPreference.numberOfRewind++
        direct = Direction.Left
        val setting = SwipeAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        manager.setSwipeAnimationSetting(setting)
        binding.cardStackView.swipe()
    }

    private fun initStackView() {
        manager = CardStackLayoutManager(requireContext(), this@HomeFragment).apply {
            setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
            setOverlayInterpolator(LinearInterpolator())
        }
        binding.cardStackView.layoutManager = manager
        binding.cardStackView.adapter = adapter
        manager.setVisibleCount(3)
        manager.setStackFrom(StackFrom.Bottom)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(65.0f)
        binding.cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }
    private fun rewind(position: Int) {
        if (UserPreference.numberOfRewind>0){
            UserPreference.numberOfRewind=0
            UserPreference.isRewind=true
            if (position > 0) { //if position is zero, that case is not handled
                val setting = RewindAnimationSetting.Builder()
                    .setDirection(Direction.Bottom)
                    .setDuration(Duration.Fast.duration)
                    .setInterpolator(DecelerateInterpolator())
                    .build()
                manager.setRewindAnimationSetting(setting)
                binding.cardStackView.rewind()
//                isRewind = false
            } else{
            }
        }else{
            Toast.makeText(requireContext() ,getString(R.string.you_can_rewind_only_for_one_user), Toast.LENGTH_SHORT).show()

        }

//        }
    }
    override fun onCardDragging(direction: Direction?, ratio: Float, position: Int) {
        direct = direction!!
        if (cardButtonsChk == 1) {
            cardButtonsChk = 0
            adapter?.hideButtons()
            adapter?.listAdapterBinding?.get(position)?.gradientImage?.isVisible = true

        }
    }


    override fun onCardSwiped(direction: Direction?, position: Int) {
        cardButtonsChk = 1
        adapter?.showButtons()
        if ("$direct" == "Top") { }
        else if ("$direct" == "Right") {
            UserPreference.numberOfRewind++
        } else if ("$direct" == "Left") {
            UserPreference.numberOfRewind++
        }
    }

    override fun onCardRewound() {}

    override fun onCardCanceled(position: Int) {
        cardButtonsChk = 1
        adapter?.showButtons()
        adapter?.listAdapterBinding?.get(position)?.gradientImage?.isVisible = false
    }

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {
        if (!UserPreference.isRewind){  //whenever card is rewind the card after the rewind card automatically swipeLeft or swipeRight based on rewind card was swipeLeft or swipeRight, to avoid this, this check is taken
            try {
                if ("$direct" == "Right") {
                    adapterPosition = position
                    if (cardListUser[position].type=="ad"){
                        if (!cardListUser[position].bannerLink.isNullOrEmpty()){
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cardListUser[position].bannerLink)))
                        }
                    }else{
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                homeViewModel.hitLikeDislikeUserApi(
                                    it,
                                    LikeDislikeRequest("like"),
                                    cardListUser[position]._id!!
                                )
                            }
                    }

                } else if ("$direct" == "Top") {
                    adapterPosition = position

                } else if ("$direct" == "Left") {
                    adapterPosition = position
                    if (cardListUser[position].type=="ad"){

                    }else{
                        Preferences.getStringPreference(requireContext(), TOKEN)
                            ?.let {
                                homeViewModel.hitLikeDislikeUserApi(
                                    it,
                                    LikeDislikeRequest("dislike"),
                                    cardListUser[position]._id!!
                                )
                            }
                    }
                } else {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            UserPreference.isRewind=false
        }
    }

    override fun callBack(id: Int) {}
    fun getMimeType(file: File): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

    private fun hitUploadStoryApi(galleryImagePathList: ArrayList<String>) {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        for ((index, path) in galleryImagePathList?.withIndex()!!) {
            val partName = "media"
            Log.i("TAG", "hitUploadStoryApi: $path")
            val file = File(path)
            builder.addFormDataPart(
                partName,
                "${System.currentTimeMillis()}_$index${file.name}",
                file.asRequestBody(getMimeType(file)?.toMediaTypeOrNull())
            )
        }
        val requestBody = builder.build()
        Preferences.getStringPreference(requireActivity(), TOKEN)
            ?.let {
                homeViewModel.hitUploadStoryLiveDataApi(it, requestBody)
            }
    }

    override fun getSelectedStory(position: Int, userId: String) {
//        get a check in my_story/other_story api to check if story still exists
        otherUserSelectedPosition = position - 1  //to get otheruserInfo from userlisting api
        if (position == 0 && !uploadedStory) {
            askPermission()
//            askForPermission(requireContext(), cameraPermission, this, showMessage = false)
        } else if (position == 0 && uploadedStory) {
            Preferences.getStringPreference(requireActivity(), TOKEN)
                ?.let { storyViewModel.hitMyStoryListingApi(it) }
        } else {
            Preferences.getStringPreference(requireActivity(), TOKEN)
                ?.let { storyViewModel.hitOtherStoryListingApi(it, userId) }
        }
    }

    override fun uploadStory() {
        askPermission()
//        askForPermission(requireContext(), cameraPermission, this, showMessage = false)
    }


    private fun openCameraGalleryBottomSheet() {
        val bottom = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_select_camera_gallery, null)
        bottom.setContentView(view1)
        val title = view1.findViewById<View>(R.id.title) as TextView
        val camera = view1.findViewById<View>(R.id.camera) as TextView
        val gallery = view1.findViewById<View>(R.id.gallery) as TextView
        title.text = getString(R.string.add_to_your_story)
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
                openGalleryImage()
            }
            bottom.dismiss()
        }

        video.setOnClickListener {
            if (cameraGalleryCheck == 1) {
                openCameraVideos()
            } else {
                openGalleryVideo()
            }
            bottom.dismiss()
        }
        bottom.show()
    }
    private val startForImageGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data?.clipData != null) {
                    for (i in 0 until data.clipData!!.itemCount) {
                        if (galleryImagePathList.size>5){
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.you_can_upload_upto_five_images_at_a_time),
                                Toast.LENGTH_SHORT
                            ).show()
                            break
                        }
                        val uri = data.clipData?.getItemAt(i)?.uri
                        if (CommonUtils.isValidMedia(uri!!, requireContext())) {
                            if (uri != null && CommonUtils.isImageUri(
                                    requireContext().contentResolver,
                                    uri
                                )
                            ) {
                                imagePath = java.lang.String.valueOf(
                                    CommonUtils.createFileSmall(
                                        CommonUtils.getRealPathFromDocumentUri(
                                            requireContext(),
                                            uri
                                        ), requireContext()
                                    )
                                )
                            } else {
                                imagePath = CommonUtils.getFilePath(requireContext(), uri)!!
                            }
                            if (CommonUtils.checkVideoFileSize(imagePath)<100){
                                galleryImagePathList.add(imagePath)
                                myStoryMediaPathList.add(imagePath)

                            }else{
                                Toast.makeText(requireContext(), getString(R.string.media_size_is_too_large), Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                    if (data.clipData != null)
                        hitUploadStoryApi(galleryImagePathList)
                } else {
                    val uri = data?.data
                    if (uri?.path?.isNotEmpty() == true) {
                        if (CommonUtils.isValidMedia(uri!!, requireContext())) {
                            if (uri != null && CommonUtils.isImageUri(
                                    requireContext().contentResolver,
                                    uri
                                )
                            ) {
                                imagePath = java.lang.String.valueOf(
                                    CommonUtils.createFileSmall(
                                        CommonUtils.getRealPathFromDocumentUri(
                                            requireContext(),
                                            uri
                                        ), requireContext()
                                    )
                                )
                            } else {
                                imagePath = CommonUtils.getFilePath(requireContext(), uri)!!
                            }
                            if (CommonUtils.checkVideoFileSize(imagePath)<100){
                                galleryImagePathList.add(imagePath)
                                myStoryMediaPathList.add(imagePath)
                                if (data != null)
                                    hitUploadStoryApi(galleryImagePathList)
                            }else{
                                Toast.makeText(requireContext(), getString(R.string.media_size_is_too_large), Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                }
            }
        }

    private fun openGalleryImage() {
        galleryImagePathList.clear()
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startForImageGallery.launch(intent)

    }


    private fun openGalleryVideo() {
        galleryImagePathList.clear()
        val intent = Intent()

        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startForImageGallery.launch(intent)

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

    private fun openCameraVideos() {
            val videoCapture = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoCapture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            // videoCapture.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 8491520L); // 8MB
            videoCapture.putExtra(MediaStore.Video.Thumbnails.HEIGHT, 320)
            videoCapture.putExtra(MediaStore.Video.Thumbnails.WIDTH, 240)
            val maxVideoSize = (50 * 1024 * 1024).toLong() // 50 MB
            videoCapture.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize)
            startActivityForResult(videoCapture, VideoRequestCode)
//        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {//camera_img
            if (resultCode == RESULT_OK) {
                path = ImageUtils.getInstant().createFileSmall(
                    ImageUtils.getInstant().getRealPathFromUri_(
                        requireContext(),
                        capturedImageUri
                    ), requireContext()
                ).absolutePath
                myStoryMediaPathList.add(path)
                hitUploadStoryApi(arrayListOf(path))

            }
        } else if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {//gallery_img
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            list?.forEach {
                val path = ImageUtils.getInstant().createFileSmall(
                    ImageUtils.getInstant().getRealPathFromUri_(
                        requireContext(), it
                    ), requireContext()
                ).absolutePath
                galleryImagePathList.add(path)
                myStoryMediaPathList.add(path)
            }
            if (!list.isNullOrEmpty())
                hitUploadStoryApi(galleryImagePathList)
        } else if (requestCode == VideoRequestCode) { //camera_video
            if (resultCode == RESULT_OK && data!!.data != null) {
                Log.d("MYVideo", Gson().toJson(data!!.data!!))

                val version = android.os.Build.VERSION.SDK_INT //version 30 camera video issue
                if (version > 29) {
                    saveAt = ""
                } else {
                    saveAt = Environment.DIRECTORY_DOWNLOADS
                }
//                showProgress()

                VideoCompressor.start(
                    context = requireContext(), // => This is required
                    uris = listOf(data!!.data!!), // => Source can be provided as content uris
                    isStreamable = true,
                    saveAt = saveAt,
                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {
                            // Update UI with progress value
                            requireActivity().runOnUiThread {
                                Log.d("MYVideo", "Progresss")
                            }
//                            showProgress()
                        }

                        override fun onStart(index: Int) {
                            // Compression start
                            Log.d("MYVideo", "Start")
//                            showProgress()

                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {
                            // On Compression success
                            Log.d("MYVideo", "Succeess  $size " + path)
//                            hideProgress()
                            vdo_path = path!!
                            val path = path
                            myStoryMediaPathList.add(path)
                            Log.i("TAG", "onActivityResult: " + path)
                            hitUploadStoryApi(arrayListOf(path))
                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            // On Failure
//                            hideProgress()
                            Log.d("MYVideo", "Failuure")

                        }

                        override fun onCancelled(index: Int) {
                            // On Cancelled
//                            hideProgress()
                            Log.d("MYVideo", "Cancel")

                        }

                    },
                    configureWith = Configuration(
                        quality = VideoQuality.VERY_HIGH,
                        frameRate = 60, /*Int, ignore, or null*/
                        isMinBitrateCheckEnabled = true,

                        )
                )
            }

        } else if (requestCode.toString() == FilePickerConst.REQUEST_CODE_MEDIA_DETAIL.toString()) { //gallery_vdo
            val list = data?.extras?.getParcelableArrayList<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
            list?.forEach {
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

    private fun compressvideo(filePath: Uri) {
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
//                    hideProgress()
                    vdo_path = path!!

                    galleryVideoPathList.add(vdo_path)
                    myStoryMediaPathList.add(vdo_path)
                    hitUploadStoryApi(arrayListOf(vdo_path))
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
                frameRate = 60, //Int, ignore, or null
                isMinBitrateCheckEnabled = false,
                disableAudio = false, //Boolean, or ignore
                keepOriginalResolution = false, //Boolean, or ignore,
            )
        )
    }
}