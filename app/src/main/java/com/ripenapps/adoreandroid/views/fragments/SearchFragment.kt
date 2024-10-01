package com.ripenapps.adoreandroid.views.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.DialogSubscriptionBinding
import com.ripenapps.adoreandroid.databinding.FragmentSearchBinding
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.User
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_USER_SUBSCRIBED
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SAVED_LAT
import com.ripenapps.adoreandroid.preferences.SAVED_LONG
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.enums.SelectedView
import com.ripenapps.adoreandroid.view_models.SearchViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterMapUserList
import com.ripenapps.adoreandroid.views.bottomsheets.FilterBottomsheet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(), OnMapReadyCallback {
    private var premiumPlanPrice=""
    private lateinit var billingClient: BillingClient
    private var tempchk=""
    private val planViewModel by viewModels<PlanViewModel>()
    private lateinit var locLatLong: LatLng
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var adapterUsersList:AdapterMapUserList

    private var mapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    var userList: MutableList<User> = mutableListOf()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var permissionId: Int = 2
    private var currentCity = ""
    private var isResetClicked=false


    override fun setLayout(): Int {
        return R.layout.fragment_search
    }

    override fun initView(savedInstanceState: Bundle?) {
        initBillingClient()
        setObserver()
        onClick()
    }
    private fun initBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(purchaseUpdateListener)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) { Log.i("TAG", "onBillingSetupFinished: if")
                    querySkuDetails()
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
    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.i("TAG purchaseUpdateListener", Gson().toJson(purchases))
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            /*googlePurchases = purchases
            for (purchase in purchases) {
//                serverFailedAfterEndTime=false
                handlePurchase(purchase)
            }*/
        }
    }
    private fun querySkuDetails() {
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
                    // Use the price and currency code as needed
                    Log.d("Subscription productdetails", Gson().toJson(productDetails))
                }
            } else {
                // Handle any errors in querying product details
            }
        }

    }

    private fun initMap() {
        mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap()
    }

    private fun hitSearchUserListApi() {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let {
                searchViewModel.hitUserSearchListApi(
                    it,
                    "",
                    UserPreference.filterMapListRequestKeys,
                    currentCity
                )
            }
    }

    private fun getSelectedUser(id:String){  //from bottom horizontal list of users
        findNavController().navigate(SearchFragmentDirections.searchFragmentToFromMapActivity(SelectedView.USER_DETAIL,  "0",
            "search",
            id))
    }
    private fun onClick() {
        binding.filterImage.setOnClickListener {
            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED) == "1") {
                var bottomSheetFragment = FilterBottomsheet(::getCustomDismiss)
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                binding.filterImage.isClickable=false
                Handler().postDelayed({
                    binding.filterImage.isClickable=true
                }, 1000)
            } else {
                openSubscriptionDialog()
            }
        }
        binding.searchUser.setOnClickListener {
            findNavController().navigate(SearchFragmentDirections.searchFragmentToFromMapActivity(SelectedView.SEARCH_USER, "", "", ""))
        }

    }
    private fun getCustomDismiss(check:String, lat:Double, long:Double){
        if (UserPreference.isFilterApplied) {
            UserPreference.isFilterApplied = false
            isResetClicked = check=="0"
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let {
                    searchViewModel.hitUserSearchListApi(
                        it,
                        "",
                        UserPreference.filterMapListRequestKeys,
                        ""
                    )
                }
            if (check=="1"){
                mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                locLatLong = LatLng(lat, long)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
            }
        }
    }
    private fun openSubscriptionDialog() {
//        Preferences.getStringPreference(requireContext(), TOKEN)
//            ?.let { planViewModel.hitPlanListApi(it) }
        createSubscriptionDialog(premiumPlanPrice)
    }

    private fun setObserver() {
        planViewModel.getPlanListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                   createSubscriptionDialog(it.data.data?.plan?.get(1)?.discountPrice.toString())

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

        searchViewModel.getUserSearchListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        200 -> {
                            Log.i("TAG", "setObserver: " + "getUserSearchListLiveData")
                            it.data?.message?.let { it1 ->
                                Log.i("TAG", "setObserver: " + it.data?.data)
                                userList.clear()
                                mMap?.clear()
                                if (!UserPreference.filterMapListRequestKeys.city.isNullOrEmpty()){
//                                    locLatLong = LatLng(UserPreference.filterMapListRequestKeys.lat, UserPreference.filterMapListRequestKeys.long)
                                }else{
                                    if(!isResetClicked){
                                        if (CommonUtils.checkPermissions(requireContext())) {
                                            if (CommonUtils.isLocationEnabled(requireActivity())) {
                                                if(::locLatLong.isInitialized){
                                                    mMap?.addMarker(MarkerOptions().position(
                                                        locLatLong).title("").snippet("")
                                                        .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.current_location_icon)))
                                                }
                                            }
                                        }
                                    }
                                }

                                userList = it.data?.data?.user!!
                                userList.forEach { userData ->
                                    setMarkerPhoto(userData)
                                }
                                adapterUsersList = AdapterMapUserList(it.data?.data?.user, ::getSelectedUser)
                                binding.usersListRecycler.adapter = adapterUsersList
                                /*if (recentSearchList.size>recentSearchListPosition){
                                    Preferences.getStringPreference(requireContext(), TOKEN)
                                        ?.let { userDetailViewModel.hitUserDetailApi(it, recentSearchList[recentSearchListPosition]) }
                                    recentSearchListPosition++
                                }*/
                            }
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
    }

    private fun createSubscriptionDialog(price:String) {
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
            tempchk="2222"
            findNavController().navigate(SearchFragmentDirections.searchToChoosePlan())
//                                        findNavController().navigate(HomeFragmentDirections.homeToChoosePlan())
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (isAdded){
            if (CommonUtils.checkPermissions(requireContext())) {
                if (CommonUtils.isLocationEnabled(requireActivity())) {
                    mFusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireContext())
                    Log.i("TAG", "getLocation: mFusedLocationClient $mFusedLocationClient")
                    mFusedLocationClient?.lastLocation?.addOnCompleteListener(requireActivity()) { task ->
                        val location: Location? = task.result
                        Log.i("TAG", "getLocation: $task")
                        Log.i("TAG", "getLocation: " + task.result)

                        if (location != null) {
                            try {
                                if (isAdded){
                                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                                    val list: List<Address> =
                                        geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                                    if (list.isNotEmpty()) {
                                        binding.apply {
                                            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                                            if (!UserPreference.filterMapListRequestKeys.city.isNullOrEmpty()){
                                                locLatLong = LatLng(UserPreference.filterMapListRequestKeys.lat, UserPreference.filterMapListRequestKeys.long)
                                            }else{
                                                locLatLong = LatLng(list[0].latitude, list[0].longitude)
                                            }
                                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
                                            currentCity = list[0].locality
                                            hitSearchUserListApi()
                                        }
                                    } else {
                                        forceFetchLocation()
                                    }
                                }

                            } catch (e: IOException) {
                                // Handle IOException (e.g., network error, timeout)
                                Toast.makeText(requireContext(), getString(R.string.network_error_try_again), Toast.LENGTH_SHORT).show()
                                e.printStackTrace()
                                // You may want to inform the user about the error or take alternative actions
                            }
                        } else {
                            forceFetchLocation()
                        }
                    }
                } else {
                    mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    if (!UserPreference.filterMapListRequestKeys.city.isNullOrEmpty()){
                        locLatLong = LatLng(UserPreference.filterMapListRequestKeys.lat, UserPreference.filterMapListRequestKeys.long)
                    }else{
                        locLatLong = LatLng(Preferences.getStringPreference(requireContext(), SAVED_LAT)?.toDouble()!!, Preferences.getStringPreference(requireContext(), SAVED_LONG)?.toDouble()!!)
                    }
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
                    currentCity=""
                    hitSearchUserListApi()
                    /*Toast.makeText(requireContext(), "Please turn on location", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, SearchLocationFragment.REQUEST_LOCATION_SETTINGS)*/
                }
            }
            else {
                mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                if (!UserPreference.filterMapListRequestKeys.city.isNullOrEmpty()){
                    locLatLong = LatLng(UserPreference.filterMapListRequestKeys.lat, UserPreference.filterMapListRequestKeys.long)
                }else{
                    locLatLong = LatLng(Preferences.getStringPreference(requireContext(), SAVED_LAT)?.toDouble()!!, Preferences.getStringPreference(requireContext(), SAVED_LONG)?.toDouble()!!)
                }
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(locLatLong, 17f))
                currentCity=""
                hitSearchUserListApi()
                /*requestPermissions()
                createInfoDialog(
                    requireContext(),
                    permissionTitle,
                    "You need to enable the location permission.!",
                    true
                )*/
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (tempchk=="2222"){

            // Get the current destination
            val currentDestination = findNavController().currentDestination

            // Pop the current destination from the back stack
            currentDestination?.id?.let { findNavController().popBackStack(it, false) }

            // Navigate to the destination again
            currentDestination?.id?.let { findNavController().navigate(it) }
        }
    }
    private fun forceFetchLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 2500
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val mostRecentLocation = locationResult.lastLocation
                if (mostRecentLocation != null) {
                    // We found a location, so we can now stop further location updates.
                    if (isAdded) {
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                            .removeLocationUpdates(this)

                        // Now that the device has a location, request it from FusedLocationProvider.
//                        requestLocationFromFusedLocationProvider()
                        getLocation()
                    }

                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return
        }
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private fun requestLocationFromFusedLocationProvider() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //Provide an additional rationale to the user. This would happen if the user denied the request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Toast.makeText(requireContext(), getString(R.string.user_denied), Toast.LENGTH_SHORT).show()

//            showUserDeniedPermissionsAlertDialog(false)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                SearchLocationFragment.REQUEST_PERMISSIONS_LOCATION_ONLY_REQUEST_CODE
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SearchLocationFragment.REQUEST_LOCATION_SETTINGS) {
            Log.i("TAG", "onActivityResult: " + data)
            // User has returned from location settings, check location again
            getLocation()
        }
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getLocation()
        mMap?.setOnMarkerClickListener { marker ->
            tempchk="111"
            findNavController().navigate(SearchFragmentDirections.searchFragmentToFromMapActivity(SelectedView.USER_DETAIL,  "0",
                "search",
                marker.title!!))
            true
        }
    }
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable!!.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable!!.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable!!.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    private fun setMarkerPhoto(location: User) {
        var bitmapFinal: Bitmap?
        Glide.with(this)
            .asBitmap()
            /*.apply(options)*/
            .centerCrop()
            .load(location.profileUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmapFinal = createUserBitmapFinal(resource)
                    val textBitmap = addTextToBitmap(
                        getTwoCharactersAfterDot(location?.distance.toString())!! + "km",
                        bitmapFinal!!
                    )
                    var markerOptions = MarkerOptions()
                        .position(
                            LatLng(
                                location?.location?.coordinates?.get(1)!!,
                                location?.location?.coordinates?.get(0)!!/*location!!.longitude*/
                            )
                        )
                        .title(location?._id)
                        .snippet("")
                        .icon(BitmapDescriptorFactory.fromBitmap(textBitmap!!))
                    mMap?.addMarker(markerOptions)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
    private fun addTextToBitmap(text: String, originalBitmap: Bitmap): Bitmap {
        // Create a new bitmap with text below the original bitmap
        val resultBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height + 50, // Adjust this based on your text size
            Bitmap.Config.ARGB_8888
        )

        // Draw the original bitmap onto the new bitmap
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Draw text below the original bitmap
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLACK // Set your desired text color
        paint.textSize = 35F // Set your desired text size
//        paint.typeface = Typeface.BOLD as Typeface // Set the text to bold

        val textX = originalBitmap.width / 2 - paint.measureText(text) / 2
        val textY = originalBitmap.height + 40F// Adjust this based on your layout

        canvas.drawText(text, textX, textY, paint)

        return resultBitmap
    }
    private fun createUserBitmapFinal(bitmapInicial: Bitmap?): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(   //overall size
                150,
                150,
                Bitmap.Config.ARGB_8888
            ) //change the size of the placeholder
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            val drawable: Drawable =
                resources.getDrawable(R.drawable.background_circular_1dp_border_theme_color)
            drawable.setBounds( //border size
                0,
                0,
                140,
                140
            ) //change the size of the placeholder, but you need to maintain the same proportion of the first line
            drawable.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()

            if (bitmapInicial != null) {
                val shader =
                    BitmapShader(bitmapInicial, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale: Float =
                    200 / bitmapInicial.width.toFloat()  //reduce or augment here change the size of the original bitmap inside the placehoder.
                // But you need to adjust the line bitmapRect with the same proportion
                matrix.postTranslate(5f, 5f)
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[5f, 5f, 124f + 10f] = 124f + 10f //change here too to change the size
                canvas.drawRoundRect(bitmapRect, 66f, 66f, roundPaint)
            }
        } catch (e: Exception) {
            // Handle the exception here
            e.printStackTrace()
            // You might want to provide a default behavior or placeholder in case of an error.
            // For example, you could set result to a default bitmap or log the error.
        }
        return result
    }
    fun getTwoCharactersAfterDot(input: String): String? {
        val dotIndex = input.indexOf('.')
        if (dotIndex != -1 && dotIndex + 3 <= input.length) {
            return input.substring(0, dotIndex + 3)
        }
        return input
    }
    override fun onDestroy() {
        super.onDestroy()
        mapFragment = null
    }

}