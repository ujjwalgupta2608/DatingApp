package com.ripenapps.adoreandroid.views.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.chaos.view.PinView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSettingsBinding
import com.ripenapps.adoreandroid.models.request_models.LoginRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_WELCOME_DONE
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.AppDialogListener
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.utils.createYesNoDialog
import com.ripenapps.adoreandroid.view_models.LoginViewModel
import com.ripenapps.adoreandroid.views.activities.MainActivity
import com.ripenapps.adoreandroid.views.adapters.AdapterProfileMenu
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyOtpRequest
import com.ripenapps.adoreandroid.models.response_models.VerifyOtpResponse
import com.ripenapps.adoreandroid.preferences.EMAIL
import com.ripenapps.adoreandroid.preferences.SOCIAL_TYPE
import com.ripenapps.adoreandroid.utils.createYesNoImageDialog
import com.ripenapps.adoreandroid.view_models.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var isPassHidden = true
    private var deleteAccountLiveData= SingleLiveEvent<Resources<CommonResponse>>()
    private val loginViewModel by viewModels<LoginViewModel>()
    private val forgotPasswordViewModel by viewModels<ForgotPasswordViewModel>()
    private val otpVerificationLiveData = SingleLiveEvent<Resources<VerifyOtpResponse>>()

    override fun setLayout(): Int {
        return R.layout.fragment_settings
    }

    override fun initView(savedInstanceState: Bundle?) {
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
        onClick()
        initMenuItem()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }
    private fun hitDeleteAccountApi(token:String/*, request: DeleteRequestModel*/){
        try {
            deleteAccountLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    deleteAccountLiveData.postValue(
                        Resources.success(
                            ApiRepository().deleteAccountApi(
                                "Bearer $token"/*, request*/
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    deleteAccountLiveData.postValue(Resources.error(getString(R.string.unable_to_connect_please_check_your_internet), null))

                } catch (ex: Exception) {
                    deleteAccountLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
    private fun setObserver() {
        deleteAccountLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when(it.data?.status){
                        200->{
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            UserPreference.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN)!!
                            Preferences.removeAllPreference(requireContext())
                            Preferences.setStringPreference(requireContext(), FCM_TOKEN, UserPreference.deviceToken)
                            Preferences.setStringPreference(requireContext(), IS_WELCOME_DONE, "true")
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
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
        loginViewModel.getDeleteAccountVerifyLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
//                                Toast.makeText(
//                                    requireActivity(),
//                                    "Password matched successfully.",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                Log.i("TAG", "hitDeleteApi: "/*+DeleteRequestModel( reason= if(selectedPosition<list.size-1) list[selectedPosition].name else binding.otherReason.text.toString().trim() )*/)
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let { it2 -> hitDeleteAccountApi(it2/*, DeleteRequestModel( reason= if(selectedPosition<list.size-1) list[selectedPosition].name else binding.otherReason.text.toString().trim() )*/) }
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

                Status.LOADING -> {}

                Status.ERROR -> {
                    Log.e("TAG", "initViewModel: ${it.message}")
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
        forgotPasswordViewModel.getSendOtpLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()

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
                    ProcessDialog.dismissDialog(true)

                }

            }
        }
        otpVerificationLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    ProcessDialog.dismissDialog(true)
                    it.data?.message?.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Preferences.getStringPreference(requireContext(), TOKEN)
                                    ?.let { it2 -> hitDeleteAccountApi(it2) }
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
                    ProcessDialog.dismissDialog(true)
                }
            }
        }
    }

    private fun openDeleteBottomsheet() {
        val view1: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottomsheet_delete_account, null)
        bottomSheetDialog.setContentView(view1)
        bottomSheetDialog.show()
        val passowrd = view1.findViewById<View>(R.id.password) as EditText
        val passwordEye = view1.findViewById<View>(R.id.password_eye) as ImageView
        val deleteButton = view1.findViewById<View>(R.id.deleteButton) as AppCompatTextView
        val forgotPassword = view1.findViewById<View>(R.id.forgotPassword) as TextView
        val passwordText = view1.findViewById<View>(R.id.passwordText) as TextView
        val title = view1.findViewById<View>(R.id.title) as TextView
        val passwordLayout = view1.findViewById<View>(R.id.passwordLayout) as RelativeLayout
        val pinView = view1.findViewById<View>(R.id.pinView) as PinView

        passowrd.transformationMethod =
            CommonUtils.DotPasswordTransformationMethod
        isPassHidden=true
        if (Preferences.getStringPreference(requireContext(), SOCIAL_TYPE)=="google"){
            title.text = getString(R.string.enter_verification_code)
            passwordLayout.isVisible=false
            passwordText.isVisible=false
            forgotPassword.isVisible=false
            pinView.isVisible=true
        }
//        set click actions of bottomsheet
        passowrd.setOnClickListener {

        }
        passwordEye.setOnClickListener {
            isPassHidden = if (isPassHidden) {
                passwordEye.setImageResource(R.drawable.open_eye)
                passowrd.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                false
            } else {
                passwordEye.setImageResource(R.drawable.close_eye)
                passowrd.transformationMethod =
                    CommonUtils.DotPasswordTransformationMethod
                true
            }
            passowrd.setSelection(passowrd.text.toString().length)
        }
        deleteButton.setOnClickListener {
            if (Preferences.getStringPreference(requireContext(), SOCIAL_TYPE)=="google"){
                if (pinView.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.please_enter_a_valid_otp), Toast.LENGTH_SHORT)
                        .show()
                } else if (pinView.text?.length!! < 4) {
                    Toast.makeText(requireContext(), getString(R.string.please_enter_a_valid_otp), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    bottomSheetDialog.dismiss()
//                        hit code verification api
                    hitVerifyOtpApi(pinView.text.toString())
                }
            }else{
                val validationError = validateInput(passowrd.text.toString().trim())
                if (validationError != null) {
                    Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.dismiss()
                    var loginRequest = LoginRequest()
                    loginRequest.userName =
                        Preferences.getStringPreference(requireContext(), USER_NAME).toString()
                    loginRequest.password = passowrd.text.toString().trim()
//                    update device token later.
                    loginRequest.deviceToken =
                        Preferences.getStringPreference(requireContext(), FCM_TOKEN).toString()
                    loginRequest.deviceType = "android"
                    Log.i("TAG", "openDeleteBottomsheet: "+loginRequest)
                    loginViewModel.hitDeleteAccountVerifyApi(loginRequest)
                }
            }
        }
        forgotPassword.setOnClickListener {
            bottomSheetDialog.dismiss()
//            findNavController().navigate(DeleteAccountFragmentDirections.deleteAccountToForgotPassword("deleteAccount"))
            findNavController().navigate(SettingsFragmentDirections.settingsToForgotPassword("settings"))

        }
    }

    private fun hitVerifyOtpApi(otp: String) {
        var verifyOtpRequest = VerifyOtpRequest()
        verifyOtpRequest.email = Preferences.getStringPreference(requireContext(), EMAIL)
        verifyOtpRequest.resetPasswordOTP = otp

        try {
            otpVerificationLiveData.postValue(Resources.loading(null))
            lifecycleScope.launch {
                try {
                    otpVerificationLiveData.postValue(
                        Resources.success(
                            ApiRepository().verifyOtpLoginApi(
                                verifyOtpRequest
                            )
                        )
                    )
                } catch (ex: Exception) {
                    otpVerificationLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun validateInput(password:String): String? {
        val passwordValue = password

        if (passwordValue.isNullOrEmpty()) {
            return getString(R.string.please_enter_a_password)
        }

        if (passwordValue.length < 8) {
            return getString(R.string.password_length_can_not_be_less)
        }
        return null // No validation error
    }

    private fun onClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initMenuItem() {
        val menuTitleList =
            listOf(getString(R.string.change_password), getString(R.string.terms_condition), getString(R.string.help), getString(R.string.privacy_policy), getString(R.string.change_language), getString(R.string.delete_account))
        val menuIconList = listOf(
            R.drawable.change_password,
            R.drawable.terms_and_conditions,
            R.drawable.help_icon,
            R.drawable.faq_icon,
            R.drawable.language_icon,
            R.drawable.delete_account
            /*R.drawable.logout_button*/
        )
        binding.menuItemRecycler.adapter =
            AdapterProfileMenu(menuTitleList, menuIconList = menuIconList, screen = "setting", ::getMenuItemPosition)
    }
    private fun getMenuItemPosition(position: Int) {
        when (position) {
            0 -> {
                if (Preferences.getStringPreference(requireContext(), SOCIAL_TYPE)=="google"){
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.cant_change_password_when_you_logged_in_socially),
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    findNavController().navigate(SettingsFragmentDirections.settingsToChangePassword())
                }
            }

            1 -> {
                findNavController().navigate(SettingsFragmentDirections.settingsToWebView(screenName = "termsConditions"))
            }

            2 -> {
                findNavController().navigate(SettingsFragmentDirections.settingsToHelp())
            }

            3 -> {
                findNavController().navigate(SettingsFragmentDirections.settingsToWebView(screenName = "privacyPolicy"))
            }
            4 -> {
                findNavController().navigate(SettingsFragmentDirections.settingsToSelectLanguage("settings"))
            }
            5 -> {
                createYesNoDialog(
                    object : AppDialogListener {
                        override fun onPositiveButtonClickListener(dialog: Dialog) {
//                            findNavController().navigate(SettingsFragmentDirections.settingsToDeleteAccount())
                            dialog.dismiss()
                            if (Preferences.getStringPreference(requireContext(), SOCIAL_TYPE)=="google"){
                                openSendOtpPopup()
                            }else{
                                openDeleteBottomsheet()
                            }
                        }

                        override fun onNegativeButtonClickListener(dialog: Dialog) {
                            dialog.dismiss()
                        }
                    },
                    requireContext(),
                    getString(R.string.delete_account),
                    getString(R.string.are_you_sure_all_flights_and_information_will_be_deleted_),
                    getString(R.string.delete),
                    getString(R.string.cancel),
                    2
                )
            }

            /*5 -> {
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
                    "Logout",
                    "Are you sure you want to Logout !",
                    "Yes",
                    "No"
                )

            }*/
        }
    }

    private fun openSendOtpPopup() {
        createYesNoImageDialog(
            object : AppDialogListener {
                override fun onPositiveButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                    openDeleteBottomsheet()
                }

                override fun onNegativeButtonClickListener(dialog: Dialog) {
                    dialog.dismiss()
                    var sendOtpRequest = SendOtpRequest()
                    sendOtpRequest.isDelete="true"
                    sendOtpRequest.email = Preferences.getStringPreference(requireContext(), EMAIL).toString()
                    forgotPasswordViewModel.hitSendOtpLoginApi(sendOtpRequest)
//                    hit send verification otp api
                }
            },
            requireContext(),
            getString(R.string.verification_required),
            getString(R.string.you_need_to_verify_your_registered_email_to_continue),
            getString(R.string.i_have_code),
            getString(R.string.send_code),
            2
        )
    }


}