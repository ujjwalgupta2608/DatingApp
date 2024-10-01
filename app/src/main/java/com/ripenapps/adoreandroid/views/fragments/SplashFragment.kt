package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.BuildConfig
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSplashBinding
import com.ripenapps.adoreandroid.models.response_models.checkversionresponse.CheckVersionResponse
import com.ripenapps.adoreandroid.preferences.IS_LOGIN
import com.ripenapps.adoreandroid.preferences.IS_UPDATE_LATER_PRESSED
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createSingleButtonPopup
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    private val checkVersionLiveData = SingleLiveEvent<Resources<CheckVersionResponse>>()

    override fun setLayout(): Int {
        return R.layout.fragment_splash
    }

    override fun initView(savedInstanceState: Bundle?) {}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        var shouldNavigate = true
        lifecycleScope.launchWhenResumed {
            delay(1000)
            if (shouldNavigate) {
                Log.i("TAG", "onViewCreated: "+Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE))
                if (Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE).isNullOrEmpty()){
                    Preferences.setStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE, "en")
                    CommonUtils.setLocale("en",requireActivity())
                }else{
                    CommonUtils.setLocale(Preferences.getStringPreference(requireActivity(), SELECTED_LANGUAGE_CODE).toString() ,requireActivity())
                }

               hitCheckVersionApi("android")

            }
        }
        // If the user presses the back button, set shouldNavigate to false
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                shouldNavigate = false
            }
        })
    }

    fun hitCheckVersionApi(deviceType: String) {
        try {
            checkVersionLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    checkVersionLiveData.postValue(
                        Resources.success(
                            ApiRepository().checkVersionApi(
                                deviceType
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    checkVersionLiveData.postValue(
                        Resources.error(
                            getString(R.string.unable_to_connect_please_check_your_internet),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    checkVersionLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    private fun setObserver() {
        checkVersionLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            200 -> {
                                if (it.data?.data?.versionData?.version==BuildConfig.VERSION_NAME){
                                    Preferences.setStringPreference(requireContext(), IS_UPDATE_LATER_PRESSED, "0")
                                    normalFlow()
                                }else{
                                    if (it.data?.data?.versionData?.isForce == true){
                                        forceUpdatePopup()
                                    }else{
                                        if(Preferences.getStringPreference(requireContext(), IS_UPDATE_LATER_PRESSED)=="1"){
                                            normalFlow()
                                        }else{
                                            updateAppPopup()
                                        }
                                    }
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
    }
    private fun normalFlow() {
        if (Preferences.getStringPreference(requireActivity(), IS_LOGIN) == "true") {
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
        } else {
            if (Preferences.getStringPreference(requireActivity(), IS_WELCOME_DONE) == "true"){
                findNavController().navigate(SplashFragmentDirections.splashToLogin())
//                        val intent = Intent(requireActivity(), QuestionsActivity::class.java)
//                        startActivity(intent)
            }else{
//                        findNavController().navigate(SplashFragmentDirections.splashToWelcome())
                findNavController().navigate(SplashFragmentDirections.splashToSelectLanguage("splash"))
            }
        }
    }
    private fun forceUpdatePopup() {
            createSingleButtonPopup(
                object : AppDialogListener {
                    override fun onPositiveButtonClickListener(dialog: Dialog) {
                        updateFromPlayStore(requireContext())
                        dialog.dismiss()
                    }
                    override fun onNegativeButtonClickListener(dialog: Dialog) {
                        dialog.dismiss()
                    }
                },
                requireContext(),
                getString(R.string.new_update_available),
                getString(R.string.a_new_version_of_adore_is_available_update_now_to_enjoy_latest_features),
                getString(R.string.update_now),
                "",
                1
            )
    }
    private fun updateAppPopup() {
        createYesNoDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    updateFromPlayStore(requireContext())
                    dialog.dismiss()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    Preferences.setStringPreference(requireContext(), IS_UPDATE_LATER_PRESSED, "1")
                    normalFlow()
                    dialog.dismiss()
                }
            },
            requireContext(),
            getString(R.string.new_update_available),
            getString(R.string.a_new_version_of_adore_is_available_update_now_to_enjoy_latest_features),
            getString(R.string.update_now),
            getString(R.string.later)
        )
    }
    private fun updateFromPlayStore(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)))
        }
    }

}