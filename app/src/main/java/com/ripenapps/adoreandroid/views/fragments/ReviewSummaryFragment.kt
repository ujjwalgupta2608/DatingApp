package com.ripenapps.adoreandroid.views.fragments

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentReviewSummaryBinding
import com.ripenapps.adoreandroid.models.request_models.PlanCancelRequest
import com.ripenapps.adoreandroid.models.request_models.PlanPurchaseRequest
import com.ripenapps.adoreandroid.models.request_models.PlanRenewModel
import com.ripenapps.adoreandroid.models.response_models.planList.Plan
import com.ripenapps.adoreandroid.preferences.*
import com.ripenapps.adoreandroid.utils.*
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import com.ripenapps.adoreandroid.views.adapters.AdapterChoosePlan
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ReviewSummaryFragment : BaseFragment<FragmentReviewSummaryBinding>() {
    private var premiumPlanPrice=""
    private lateinit var googlePurchases: MutableList<Purchase>
    private var purchaseToken=""
    private var serverFailedAfterEndTime=true
    private var subscriptionEndTime = ""
    private var paidPlanData= Plan()
    private var purchaseId=""
    private lateinit var billingClient: BillingClient
    private lateinit var skuDetailsList: List<SkuDetails>
    private val planViewModel by viewModels<PlanViewModel>()
//    private val clientId = "109192566865-s0gkc9kvraqf48pf9bfvbf5td1dpr1lo.apps.googleusercontent.com"

    override fun setLayout(): Int {
        return R.layout.fragment_review_summary
    }

    override fun initView(savedInstanceState: Bundle?) {
        paidPlanData = ReviewSummaryFragmentArgs.fromBundle(requireArguments()).paidPlanData
        purchaseId = ReviewSummaryFragmentArgs.fromBundle(requireArguments()).purchaseId
        premiumPlanPrice = ReviewSummaryFragmentArgs.fromBundle(requireArguments()).premiumPlanAmount
        onClick()
        initUi()
//        getDeviceCode()

        initBillingClient()
//        querySubscriptions()
    }

    private fun getDeviceCode() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val deviceAuthorizationEndpoint = "https://oauth2.googleapis.com/device/code"
//                val clientId = "YOUR_CLIENT_ID"
                val scope = "https://www.googleapis.com/auth/androidpublisher"

                val requestBody = FormBody.Builder()
                    .add("client_id",  "109192566865-kpja2qog39nci7psesfplf6uslqhhi0l.apps.googleusercontent.com")
                    .add("scope", scope)
                    .build()

                val request = Request.Builder()
                    .url(deviceAuthorizationEndpoint)
                    .post(requestBody)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                val responseBody = response.body?.string()


                println("device_code response ${Gson().toJson(responseBody)}")
//                val jsonResponse = JSONObject(responseBody)
//                val deviceCode = jsonResponse.getString("device_code")
//                val userCode = jsonResponse.getString("user_code")
//                val verificationUrl = jsonResponse.getString("verification_url")
//
//                println("User Code: $userCode")
//                println("Verification URL: $verificationUrl")
//                println("deviceCode : $deviceCode")

            } catch (e: IOException) {
                // Handle exceptions, such as network errors
                e.printStackTrace()
            }
        }
    }
    private fun getSubscriptionDetail() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val accessToken = "YOUR_ACCESS_TOKEN" // Replace with your actual access token
                val packageName = activity?.packageName // Replace with your app's package name
                val subscriptionId = "YOUR_SUBSCRIPTION_ID" // Replace with your subscription ID

                // Construct the API request URL
                val apiUrl = "https://androidpublisher.googleapis.com/v3/applications/$packageName/purchases/subscriptions/$subscriptionId"

                // Build the request with the access token in the Authorization header
                val request = Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                // Send the request
                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                // Check if the request was successful (status code 200)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Parse the response body to extract subscription details
                    // Handle the subscription details as needed
                    println("Subscription Details: $responseBody")
                } else {
                    // Handle unsuccessful response (e.g., error response)
                    println("Error: ${response.code} - ${response.message}")
                }
            } catch (e: IOException) {
                // Handle network or I/O errors
                println("IOException: ${e.message}")
            }
        }
    }
/*
    private fun getAccessToken() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val tokenEndpoint = "https://oauth2.googleapis.com/token"
                val scope = "https://www.googleapis.com/auth/androidpublisher"
                val requestBody = FormBody.Builder()
                    .add("client_id", getString(R.string.web_client_id))
                    .add("scope", scope)
                    .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                    .build()
                val request = Request.Builder()
                    .url(tokenEndpoint)
                    .post(requestBody)
                    .build()
                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                // Log the response body
                Log.i("TAG", "Response Body: $responseBody")
                // on successful, call this
//                    getSubscriptionDetail()


                // Further processing of the response...
            } catch (e: IOException) {
                // Handle exceptions, such as network errors
                Log.e("TAG", "IOException: ${e.message}", e)
            }
        }
    }
*/
    fun querySubscriptions() {
        billingClient.queryPurchasesAsync(
            BillingClient.SkuType.SUBS
        ) { billingResult: BillingResult, purchasesList: List<Purchase>? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList != null) {
                for (purchase in purchasesList) {
                    // Handle the purchased subscription
//                    val subscriptionId: String = purchase.getSku()
                    Log.i("TAG", "querySubscriptions: "+Gson().toJson(purchase))
                    // You can use subscriptionId to identify the purchased subscription
                }
            }
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }
    private fun setObserver() {
        planViewModel.getPlanCancelLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data.status) {
                            200 -> {
                                planViewModel.hitPlanRenewApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanRenewModel(paidPlanData._id, purchaseToken, subscriptionEndTime))
                                Log.i("TAG", "setObserver: Plan cancel successful.")
                            }
                            422->{
                                planViewModel.hitPlanRenewApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanRenewModel(paidPlanData._id, purchaseToken, subscriptionEndTime))
                                Log.i("TAG", "setObserver: Plan cancel successful.")
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
        planViewModel.getPlanPurchaseLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data.status) {
                            200 -> {
//                                Toast.makeText(
//                                    requireActivity(),
//                                    it1,
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "1")
                                findNavController().navigate(ReviewSummaryFragmentDirections.reviewSummaryToPaymentCompleted())
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
        planViewModel.getPlanRenewLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data.status) {
                            200 -> {
//                                Toast.makeText(
//                                    requireActivity(),
//                                    it1,
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                Preferences.setStringPreference(requireContext(), IS_USER_SUBSCRIBED, "1")
                                findNavController().navigate(ReviewSummaryFragmentDirections.reviewSummaryToPaymentCompleted())
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
    }

    private fun initUi() {
        binding.amount.text = /*"R "+paidPlanData.discountPrice.toString()*/premiumPlanPrice
        binding.taxAmount.text="0"
        binding.totalAmount.text = /*"R "+paidPlanData.discountPrice.toString()*/premiumPlanPrice
        binding.paidPlanDesc.text= /*Html.fromHtml(paidPlanData.description).trim()*/getString(R.string.go_pro_get_more_benefits)
        binding.paidPlanRecycler.adapter = AdapterChoosePlan(paidPlanData)
//        if (Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)=="ar"){
//            binding.paidPlanAmount.text = premiumPlanPrice
//            binding.perMonth.text = "${getString(R.string.month)} / "
//        }else{
            binding.paidPlanAmount.text = premiumPlanPrice
            binding.perMonth.text = " / ${getString(R.string.month)}"
//        }
//        binding.paidPlanAmount.text = /*"R "+paidPlanData.discountPrice.toString()*/premiumPlanPrice
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.confirmPayment.setOnClickListener {
//            Log.i("TAG", "onClick: paidPlanRecycler")
            if (UserPreference.anotherUserSubscribedFromThisPlayStore=="1"){
                subscribedFromAnotherAccountPopup()
            }else{
                purchaseSubscription(requireActivity())
            }
//            findNavController().navigate(ReviewSummaryFragmentDirections.reviewSummaryToPaymentCompleted())
        }
    }

    private fun subscribedFromAnotherAccountPopup() {
        createSingleButtonPopup(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.wait),
            getString(R.string.active_subscription_detected_on_this_play_store_),
            getString(R.string.ok_got_it),
            "",
            1
        )
    }

    private fun querySkuDetails() {
        val skuList = listOf("adore_product1234")/*adore_product1234*/
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        /*val productList = QueryProductDetailsParams.Product.newBuilder()
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
                    val subscriptionOffer = productDetails.subscriptionOfferDetails?.firstOrNull()
                    val pricingPhase = subscriptionOffer?.pricingPhases?.pricingPhaseList?.firstOrNull()
                    val price = pricingPhase?.formattedPrice
                    val currencyCode = pricingPhase?.priceCurrencyCode

                    // Use the price and currency code as needed
                    Log.d("Subscription Price", "Price: $price, Currency: $currencyCode")
                    Log.d("Subscription productdetails", Gson().toJson(productDetails))
                }
            } else {
                // Handle any errors in querying product details
            }
        }*/
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null) {
                    println("Checking details $skuDetailsList")
                    this.skuDetailsList = skuDetailsList
                }
            } else {
                Log.d("TAG", "querySkuDetails: $skuDetailsList")
                // Handle query failure
            }
        }
    }

    private fun purchaseSubscription(activity: Activity) {
        if(::skuDetailsList.isInitialized){
            val skuDetails = skuDetailsList.firstOrNull()
            val flowParams = skuDetails?.let {
                BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()
            }
            if (flowParams != null) {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }else{
            Toast.makeText(requireContext(), getString(R.string.try_again_later), Toast.LENGTH_SHORT).show()
        }
    }
    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.i("TAG purchaseUpdateListener", Gson().toJson(purchases))
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            googlePurchases = purchases
            for (purchase in purchases) {
//                serverFailedAfterEndTime=false
                handlePurchase(purchase)
            }
        }
    }
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("TAG", "debugMessage: ${billingResult.debugMessage}")
                        Log.i("TAG", "acknowledgePurchaseFullResult: $purchase")
                        subscribeUser(purchase)

                    } else {
                        Log.d("TAG", "handlePurchase: ${billingResult.debugMessage}")
                        // Handle acknowledgment failure
                        if (serverFailedAfterEndTime){
                            for (purchase in googlePurchases) {
                                subscribeUser(purchase)
                            }
                        }
                        /*if (serverFailedAfterEndTime){
                            if (UserPreference.hasUserSubscribedBefore=="0"){
                                planViewModel.hitPlanPurchaseApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanPurchaseRequest(paidPlanData._id, purchase.purchaseToken, subscriptionEndTime))
                            }else{
                                planViewModel.hitPlanCancelApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanCancelRequest(purchaseId = purchaseId, status = "true"))
                            }
                            Snackbar.make(binding.title, "Please do not close the app. Your transaction is being processed.", Snackbar.LENGTH_SHORT).show()
                        }*/
                    }
                }
            } else {
                // Purchase is already acknowledged
                // Provide content to user
            }
        }
    }

    private fun subscribeUser(purchase: Purchase) {
        // Calculate subscription start and end times based on purchase time and subscription period
        // Assuming subscription period is in milliseconds (e.g., 30 days)
//                        30 * 24 * 60 * 60 * 1000L // 30 days in milliseconds  -->  replace this with 5 min after testing mode changed to live
//                        val subscriptionEndTimeMillis = purchase.purchaseTime + /*30 * 24 * 60*/ 5 * 60 * 1000L // 5 min in milliseconds
        val calendar = Calendar.getInstance()
        calendar.time = Date(purchase.purchaseTime)
        calendar.add(Calendar.MONTH, 1)
        // Convert start time and end time to date with a specific time format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        subscriptionEndTime = dateFormat.format(calendar.time)
        Log.i("TAG", "subscriptionEndTime: $subscriptionEndTime")
        purchaseToken = purchase.purchaseToken
//        serverFailedAfterEndTime=true
        if (UserPreference.hasUserSubscribedBefore=="0"){
            planViewModel.hitPlanPurchaseApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanPurchaseRequest(paidPlanData._id, purchase.purchaseToken, subscriptionEndTime))
        }else{
            planViewModel.hitPlanCancelApi(Preferences.getStringPreference(requireContext(), TOKEN).toString(), PlanCancelRequest(purchaseId = purchaseId, status = "true"))
        }
        Snackbar.make(binding.title, getString(R.string.please_do_not_close_the_app_your_), Snackbar.LENGTH_SHORT).show()
        serverFailedAfterEndTime=false

    }

    // Method for verifying purchase on your server
    private fun verifyPurchase(purchase: Purchase) {
        // Implement verification logic
    }
    // Method for handling subscription renewals and cancellations
    private fun handleSubscriptionEvents(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            // Subscription is pending
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            // Subscription state is unspecified
        } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Subscription purchased
            if (purchase.isAutoRenewing) {
                // Subscription is auto-renewing
            } else {
                // Subscription is not auto-renewing (cancelled)
            }
        }
    }
}