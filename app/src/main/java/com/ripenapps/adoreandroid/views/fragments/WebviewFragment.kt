package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentWebviewBinding
import com.ripenapps.adoreandroid.models.response_models.TermsAndPolicyResponse
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.MyWebViewClient
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import kotlinx.coroutines.launch
import java.io.IOException


class WebviewFragment : BaseFragment<FragmentWebviewBinding>() {
    var screenName=""
    private var termsAndPolicyLiveData= SingleLiveEvent<Resources<TermsAndPolicyResponse>>()
    override fun setLayout(): Int {
        return R.layout.fragment_webview
    }

    override fun initView(savedInstanceState: Bundle?) {
        screenName= arguments?.getString("screenName").toString()
        onClick()
        setupUi()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setObserver() {
        termsAndPolicyLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        200->{
                            binding.webView.loadData(
                                it.data?.data?.content?.description!!,"text/html",
                                "UTF-8")
                            /*it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }*/

                        }
                        else->{
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

    private fun onClick() {
        binding.backButton.setOnClickListener {
              findNavController().popBackStack()
        }
    }

    private fun setupUi() {
        if (screenName=="termsConditions"){
            binding.title.text = getString(R.string.terms_condition)
//            binding.webView.getSettings().setJavaScriptEnabled(true)
//            binding.webView.getSettings().setDomStorageEnabled(true)
//            binding.webView.setWebViewClient(object : WebViewClient() {
//                override fun onPageFinished(view: WebView, url: String) {
//                    binding.webView.evaluateJavascript(
//                        "(function() {" +
//                                "   var links = document.getElementsByTagName('a');" +
//                                "   for (var i = 0; i < links.length; i++) {" +
//                                "       links[i].style.color = 'blue';" +
//                                "       links[i].style.textDecoration = 'underline';" +
//                                "   }" +
//                                "})()", null
//                    )
//                }
//            })
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                WebView.setWebContentsDebuggingEnabled(true)
//            }
            binding.webView.webViewClient = MyWebViewClient()
            binding.webView.loadUrl("https://adore-dating.com:6870/user/cms/terms")

//            binding.webView.loadUrl("http://13.235.137.221:6870/user/cms/terms")
//            Preferences.getStringPreference(requireContext(), TOKEN)
//                ?.let { hitTermsAndPolicyApi(it, "terms") }
        } else if (screenName=="privacyPolicy"){
            binding.title.text = getString(R.string.privacy_policy)
            binding.webView.webViewClient = MyWebViewClient()
            binding.webView.loadUrl("https://adore-dating.com:6870/user/cms/policy")
//            Preferences.getStringPreference(requireContext(), TOKEN)
//                ?.let { hitTermsAndPolicyApi(it, "policy") }
        }
    }

    private fun hitTermsAndPolicyApi(token:String, type:String) {
        try {
            termsAndPolicyLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    termsAndPolicyLiveData.postValue(
                        Resources.success(
                            ApiRepository().termsAndPolicyApi(
                                "Bearer $token", type
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    termsAndPolicyLiveData.postValue(Resources.error(getString(R.string.unable_to_connect_please_check_your_internet), null))

                } catch (ex: Exception) {
                    termsAndPolicyLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
