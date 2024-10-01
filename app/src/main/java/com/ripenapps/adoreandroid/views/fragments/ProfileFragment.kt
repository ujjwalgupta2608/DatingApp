package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.DialogSubscriptionBinding
import com.ripenapps.adoreandroid.databinding.FragmentProfileBinding
import com.ripenapps.adoreandroid.preferences.*
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.MyApplication
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createBoostAvailableDialog
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.LoginViewModel
import com.ripenapps.adoreandroid.view_models.MyProfileViewModel
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterProfileMenu
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private var premiumPlanPrice=""
    private lateinit var billingClient: BillingClient
    private val MAX_BOOST_TIME: String = "0.31"
    private val profileViewModel by viewModels<MyProfileViewModel>()
    private var job: Job? = null
    private val loginViewModel by viewModels<LoginViewModel>()
    private lateinit var socket: Socket
    private val planViewModel by viewModels<PlanViewModel>()

    override fun setLayout(): Int {
        return R.layout.fragment_profile
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
    override fun initView(savedInstanceState: Bundle?) {
        initBillingClient()
        onClick()
        setSubscriptionBanner()
        if (isAdded) {
            initializeSocket()
        }
        initMenuItem()
    }


    private fun setSubscriptionBanner() {
        if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
            binding.getPremiumPlan.text = getString(R.string.you_are_a_subscribed_user)
            binding.planDescription.text = "${getString(R.string.expires_on)} ${CommonUtils.convertTimeToDateFormat(Preferences.getStringPreference(requireContext(), PLAN_EXPIRE_DATE).toString(), "dd-MMM-yyyy")}."
            binding.premiumBannerNextIcon.isVisible=false
//            binding.premiumPlanLayout.isClickable=false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setObserver()
    }
    private fun initializeSocket() {
        val app = requireActivity().application as MyApplication
        socket = app.getSocket()
    }
    private fun emitDisconnectUser() {
        socket.emit("disconnectUser")
    }
    private fun setObserver() {
        planViewModel.getPlanListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                openSubscriptionDialog(it.data.data?.plan?.get(1)?.discountPrice.toString())
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
                            binding.boostButton.isVisible=false
                            binding.boostTimer.isVisible = true
                            UserPreference.boostCount = it.data?.data?.response?.data?.boostCount
                            UserPreference.endTime=it.data.data?.response?.data?.endTime
                            Toast.makeText(requireContext(), getString(R.string.your_profile_is_boosted_for_next_30_min), Toast.LENGTH_SHORT).show()
                            binding.boostTimer.text = MAX_BOOST_TIME
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
//                    binding.idLoadar.root.visibility = View.GONE


                }

            }
        }
        loginViewModel.getLogoutLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                ProcessDialog.dismissDialog(true)
                                emitDisconnectUser()
                                UserPreference.clear()
                                UserPreference.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)!!
                                UserPreference.savedLanguageCode= Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)!!
                                Preferences.removeAllPreference(requireContext())
                                Preferences.setStringPreference(requireContext(), FCM_TOKEN, UserPreference.deviceToken)
                                Preferences.setStringPreference(requireContext(), SELECTED_LANGUAGE_CODE, UserPreference.savedLanguageCode)
                                Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
                                if (Places.isInitialized()){
                                    Places.deinitialize()
                                }
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
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
    }
    override fun onResume() {
        super.onResume()
        setLocation()
    }
    private fun setLocation() {
        binding.locationText.text = Preferences.getStringPreference(requireContext(), CITY)+", "+Preferences.getStringPreference(requireContext(), COUNTRY)
    }
    private fun setupUi() {
        binding.name.text = Preferences.getStringPreference(requireContext(), NAME)
        binding.userName.text = Preferences.getStringPreference(requireContext(), USER_NAME)
        Glide.with(requireContext())
            .load(Preferences.getStringPreference(requireContext(), PROFILE))
            .into(binding.profileImage)
        binding.premiumIconInProfile.isVisible =
            Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"
        checkBoost()
    }

    private fun initMenuItem() {
        val menuTitleList =
            listOf(getString(R.string.my_profile), getString(R.string.settings), getString(R.string.faq), getString(R.string.invites_friends), getString(R.string.logout))
        val menuIconList = listOf(
            R.drawable.my_profile_icon,
            R.drawable.settings_icon,
            R.drawable.faq_icon,
            R.drawable.invite_friends_icon,
            R.drawable.logout_button
        )
        binding.menuItemRecycler.adapter =
            AdapterProfileMenu(menuTitleList, menuIconList = menuIconList, screen = "profile", ::getMenuItemPosition)
    }

    private fun getMenuItemPosition(position: Int) {
        when (position) {
            0 -> {
                findNavController().navigate(ProfileFragmentDirections.profileToEditMyProfile())
            }

            /*1 -> {
                findNavController().navigate(ProfileFragmentDirections.profileToPayment())
            }*/

            1 -> {
                findNavController().navigate(ProfileFragmentDirections.profileToSettings())
            }

            2 -> {
                findNavController().navigate(ProfileFragmentDirections.profileToFAQ())
            }

            3 -> {
                findNavController().navigate(ProfileFragmentDirections.profileToInviteFriends())
            }

            4 -> {
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                            Preferences.getStringPreference(requireContext(), TOKEN)
                                ?.let { loginViewModel.hitUserLogoutApi(it) }
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    getString(R.string.logout),
                    getString(R.string.are_you_sure_you_want_to_logout),
                    getString(R.string.yes),
                    getString(R.string.no)
                )


            }
            6->{
                Preferences.getStringPreference(requireContext(), TOKEN)
                    ?.let { planViewModel.hitPlanListApi(it) }
//                showGetSubscriptionDialog()
            }
        }
    }

    private fun showGetSubscriptionDialog() {
        /*var dialog=Dialog(requireContext())
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
        }*/

    }

    private fun onClick() {
        binding.notification.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.profileToNotification())
        }
        binding.logoutButtonHome.setOnClickListener {
            Preferences.removeAllPreference(requireContext())
            Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        binding.premiumPlanLayout.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.profileToChoosePlan())
        }
        binding.locationText.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.profileToSearchLocation())
        }
        binding.boostButton.setOnClickListener {
            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                if (UserPreference.boostCount!! >0){
                    openBoostPopup()
                }else{
                    // all boosts consumed, subscribe again
                    boostsFinishedPopup()
                }
            } else{
                openSubscriptionDialog(premiumPlanPrice)
//                Preferences.getStringPreference(requireContext(), TOKEN)
//                    ?.let { planViewModel.hitPlanListApi(it) }
//                showGetSubscriptionDialog()
            }
        }
        binding.boostTimer.setOnClickListener {
            if (CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                Log.i("TAG", "isBoosted: "+ CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                showRemainingBoostPopup()
            }else{
                checkBoost()
            }
        }
    }

    private fun openSubscriptionDialog(premiumPrice:String) {
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
        dialogBinding.subscriptionAmount=premiumPrice
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
            findNavController().navigate(ProfileFragmentDirections.profileToChoosePlan())
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
    override fun onPause() {
        super.onPause()
        job?.cancel()
    }
    private fun checkBoost(){   //when this fragment opens
        job = CoroutineScope(Dispatchers.Main).launch {
            repeat(140) { // Repeat every 15 seconds
                if(isAdded){
                    if (!UserPreference.endTime.isNullOrEmpty()&&Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                        if (CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")){
                            Log.i("TAG", "isBoosted: "+ CommonUtils.compareDates(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                            binding.boostButton.isVisible=false
                            binding.boostTimer.isVisible = true
                            binding.boostTimer.text = "0:"+(CommonUtils.subractTimes(UserPreference.endTime!!, CommonUtils.getCurrentTimeInUTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")+1).toString()
                        }else{
                            binding.boostButton.isVisible=true
                            binding.boostTimer.isVisible = false
                        }
                    }else{
                        binding.boostButton.isVisible=true
                        binding.boostTimer.isVisible = false
                    }
                    delay(10000) // 15 seconds delay
                }
            }
        }
    }


    private fun hitBoostApi() {   //when profile is boosted from boost button
        if (UserPreference.boostCount!!>0){
            Preferences.getStringPreference(requireContext(), TOKEN)
                ?.let { profileViewModel.hitBoostProfileApi(it) }

        }else{
            binding.boostButton.isVisible=true
            binding.boostTimer.isVisible = false
            Toast.makeText(requireContext(), getString(R.string.no_boost_left), Toast.LENGTH_SHORT).show()
        }
    }
}