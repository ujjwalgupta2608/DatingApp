package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.gson.Gson
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentChoosePlanBinding
import com.ripenapps.adoreandroid.models.response_models.planList.Plan
import com.ripenapps.adoreandroid.preferences.*
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createSingleButtonPopup
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.PlanViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterChoosePlan
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@AndroidEntryPoint
class ChoosePlanFragment : BaseFragment<FragmentChoosePlanBinding>()/*, PurchasesUpdatedListener*/ {
    private var premiumPlanPrice=""
    private var purchaseId=""
    private lateinit var billingClient: BillingClient
    //    private var productList: List<QueryProductDetailsParams.Product> = listOf()
    private val planViewModel by viewModels<PlanViewModel>()
    private var paidPlanData:Plan=Plan()
    override fun setLayout(): Int {
        return R.layout.fragment_choose_plan
    }

    override fun initView(savedInstanceState: Bundle?) {
        initBillingClient()
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
                    hitPlanListApi()
                    // Use the price and currency code as needed
                    Log.d("Subscription productdetails", Gson().toJson(productDetails))
                }
            } else {
                // Handle any errors in querying product details
            }
        }

    }
    private fun getSubscriptionDetails() {
        lifecycleScope.launch {
            try {
                val accessToken = /*getAccessToken()*/""
                Log.i("TAG", "initView: $accessToken")

                val packageName = activity?.packageName // Replace with your app's package name
                val subscriptionId = "adore_product1234" // Replace with your subscription ID
                val purchaseToken = "egkbiegdaolefallgckebaph.AO-J1Oxm1nYAWSBAwOV3kvAmB8Qp1vUl_ZKxowt8L5LUllFV0FGZ0eG0I8xqd4ClvwoWRtHEHUBjih-1-OPdzehrcZurhseEW7pE3UUKfxt_WfT-We8mtgE"

                // Construct the API request URL
                val apiUrl = "https://androidpublisher.googleapis.com/v3/applications/$packageName/purchases/subscriptions/$subscriptionId/tokens/$purchaseToken"

                // Build the request with the access token in the Authorization header
                val request = Request.Builder()
                    .url(apiUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                // Send the request in a coroutine context
                val client = OkHttpClient()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                // Check if the request was successful (status code 200)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Parse the response body to extract subscription details
                    // Handle the subscription details as needed
                    Log.i("TAG","Subscription Details: $responseBody")
                } else {
                    // Handle unsuccessful response (e.g., error response)
                    Log.i("TAG","Error: ${response.code} - ${response.message}")
                }
                /*val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.googleapis.com/androidpublisher/v3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(SubscriptionApiService::class.java)

                val response = api.getSubscription(
                    "com.ripenapps.adoreandroid",
                    "adore_product1234",
                    "icnfbjdihbkejmndbhlapkdc.AO-J1Ox75rgdzn98H73kf_iL387nSfGMtZH-v4IHa6GA5peVDTtJXjbpE39iHqLV02aX4zSvs6pGVNRxBDDZGu9AOQEg5Ph44SBwCb94ggPutlL9ODxy1Oc",
                    "Bearer $accessToken"
                )
                Log.i("TAG", "getSubscription response: "+response)
                if (response.isSuccessful) {
                    val subscription = response.body()
                    subscription?.let {
                        Log.i("TAG", "subscription response: "+ Gson().toJson(subscription))
                        // Use the expiry time
                    }
                } else {
                    // Handle error
                    Log.e("TAG", "Error: ${Gson().toJson(response)}")
                }*/
            } catch (e: Exception) {
                // Handle failure or exception
                Log.e("TAG", "Exception: ${e.message}", e)
            }
        }

    }

    /*suspend fun getAccessToken(): String {
        return withContext(Dispatchers.IO) {
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val httpTransport = NetHttpTransport()

            val inputStream: InputStream = requireContext().resources.openRawResource(R.raw.service_account)

            val credential = GoogleCredential.fromStream(inputStream, httpTransport, jsonFactory)
                .createScoped(listOf("https://www.googleapis.com/auth/androidpublisher"))

            credential.refreshToken()
            credential.accessToken
        }
    }*/

/*
    private fun billingClientStateListener() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.i("TAG", "onBillingSetupFinished: 123")
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }
*/

    private fun hitPlanListApi() {
        Preferences.getStringPreference(requireContext(), TOKEN)
            ?.let { planViewModel.hitPlanListApi(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        planViewModel.getPlanListLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                binding.freePlanRecyclerLayout.isVisible=true
                                binding.paidPlanRecyclerLayout.isVisible=true
                                paidPlanData=it.data?.data?.plan?.get(1)!!
                                purchaseId= it.data?.data?.purchase?._id.toString()
                                if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="1"){
                                    binding.freePlanDesc.text=""
                                    binding.paidPlanDesc.text=getString(R.string.current_plan)
                                }else{
                                    binding.freePlanDesc.text=getString(R.string.current_plan)
                                    binding.paidPlanDesc.text= getString(R.string.go_pro_get_more_benefits)
                                }
//                                if (Preferences.getStringPreference(requireContext(), SELECTED_LANGUAGE_CODE)=="ar"){
//                                    binding.paidPlanAmount.text = /*"R "+it.data.data?.plan?.get(1)?.discountPrice.toString()*/premiumPlanPrice
//                                    binding.perMonth.text = "${getString(R.string.month)} / "
//                                }else{
                                    binding.paidPlanAmount.text = /*"R "+it.data.data?.plan?.get(1)?.discountPrice.toString()*/premiumPlanPrice
                                    binding.perMonth.text = " / ${getString(R.string.month)}"
//                                }
                                binding.freePlanRecycler.adapter = AdapterChoosePlan(it.data.data?.plan?.get(0)!!)
                                binding.paidPlanRecycler.adapter = AdapterChoosePlan(it.data.data?.plan[1]!!)
                            }
                            403->{

                                it.data.message?.let { it1 ->
                                    Toast.makeText(
                                        requireContext(),
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

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.paidPlanRecycler.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="0"){
                    findNavController().navigate(ChoosePlanFragmentDirections.choosePlanToReviewSummary(paidPlanData, purchaseId, premiumPlanPrice))
                }else{
                    if (UserPreference.userSubscribedFromAnotherPlayStore=="1"){
                        openUserSubscribedFromAnotherPlayStorePopup()
                    }else{
                        openCancellationDialog()
                    }
                }
                true
            } else false
        })
        binding.paidPlanRecyclerLayout.setOnClickListener { //this and paidPlanRecycler.setOnTouchListener have same click event
            if (Preferences.getStringPreference(requireContext(), IS_USER_SUBSCRIBED)=="0"){
                findNavController().navigate(ChoosePlanFragmentDirections.choosePlanToReviewSummary(paidPlanData, purchaseId, premiumPlanPrice))
            }else{
                if (UserPreference.userSubscribedFromAnotherPlayStore=="1"){
                    openUserSubscribedFromAnotherPlayStorePopup()
                }else{
                    openCancellationDialog()
                }
            }
        }
    }

    private fun openUserSubscribedFromAnotherPlayStorePopup() {
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
            getString(R.string.already_subscribed),
            getString(R.string.this_account_is_already_subscribed_from),
            getString(R.string.ok),
            "",
            1
        )
    }

    private fun openCancellationDialog() {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.you_can_cancel_it_from_playstore),
                        Toast.LENGTH_SHORT
                    ).show()
                    openPlayStore()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.cancel_subscription),
            getString(R.string.are_you_sure_you_want_to_cancel),
            getString(R.string.yes_cancel),
            getString(R.string.no),
            2
        )
    }
    private fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://play.google.com/store/account/subscriptions")
        startActivity(intent)

    }
    /*    private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }

        fun showProducts(){
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        ImmutableList.of(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("product_id_example")
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()))
                    .build()

            billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                    billingResult,
                    productDetailsList ->
                // check billingResult
                // process returned productDetailsList
            }
        }

        suspend fun processPurchases() {
            productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("product_id_example")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            // leverage queryProductDetails Kotlin extension function
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params.build())
            }
            // Process the result.
        }
        fun launchPurcaseFlow(){
                // An activity reference from which the billing flow will be launched.
    //            val activity : Activity = ...;

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                        .setProductDetails(productDetails)
                        // For One-time product, "setOfferToken" method shouldn't be called.
                        // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                        // for a list of offers that are available to the user
                        .setOfferToken(selectedOfferToken)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

        // Launch the billing flow
                val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
            }

       override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (p0.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }*/


}