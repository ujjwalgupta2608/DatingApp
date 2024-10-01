package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentForgotPasswordBinding
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.ForgotPasswordViewModel

class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {
    private val viewModel by viewModels<ForgotPasswordViewModel>()
    var previousScreen=""
    override fun setLayout(): Int {
        return R.layout.fragment_forgot_password
    }


    fun validateMobile(): String? {
        if (viewModel.mobileNumber.get().isNullOrEmpty()) {
            return getString(R.string.please_enter_a_valid_number_to_proceed)
        }

        if (viewModel.mobileNumber.get()!!.length < 8) {
            return getString(R.string.please_enter_a_valid_mobile_number)
        }
        return null // No validation error
    }

    fun validateEmail(): String? {

        if (viewModel.mobileNumber.get().isNullOrEmpty()) {
            return getString(R.string.please_enter_a_valid_email_to_proceed)
        }

        if (!isValidEmail(viewModel.mobileNumber.get()!!)) {
            return getString(R.string.invalid_email_format_please_enter_a_valid_email_to_proceed)
        }

        return null // No validation error
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun hitSendOtpLogin() {
        if (viewModel.emailVisibilityChk.value == true) {
            val validationError = validateEmail()
            if (validationError != null) {
                Toast.makeText(requireActivity(), validationError, Toast.LENGTH_SHORT).show()
            } else {
                var sendOtpRequest = SendOtpRequest()
                UserPreference.emailAddress = viewModel.mobileNumber.get()!!
                sendOtpRequest.email = viewModel.mobileNumber.get()!!
                viewModel.hitSendOtpLoginApi(sendOtpRequest)
            }
        } else if (viewModel.emailVisibilityChk.value == false) {
            val validationError = validateMobile()
            if (validationError != null) {
                Toast.makeText(requireActivity(), validationError, Toast.LENGTH_SHORT).show()
            } else {
                var sendOtpRequest = SendOtpRequest()
                UserPreference.mobileNumber = viewModel.mobileNumber.get()!!
                UserPreference.countryCode = "+"+binding.countryCode.selectedCountryCode
                sendOtpRequest.mobile = viewModel.mobileNumber.get()!!
                sendOtpRequest.countryCode = "+"+binding.countryCode.selectedCountryCode
                viewModel.hitSendOtpLoginApi(sendOtpRequest)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.getSendOtpLiveData().observe(viewLifecycleOwner) {
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
                                if (viewModel.emailVisibilityChk.value == true) {
                                    findNavController().navigate(
                                        ForgotPasswordFragmentDirections.forgotPasswordToLoginCodeVerification(
                                            "forgotWithEmail", previousScreen
                                        )
                                    )
                                } else {
                                    findNavController().navigate(
                                        ForgotPasswordFragmentDirections.forgotPasswordToLoginCodeVerification(
                                            "forgotWithMobile", previousScreen
                                        )
                                    )
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
                    ProcessDialog.dismissDialog(true)

                }

            }
        }

    }

    override fun initView(savedInstanceState: Bundle?) {
        previousScreen = ForgotPasswordFragmentArgs.fromBundle(requireArguments()).previousScreen
        onClick()
    }

    private fun onClick() {
        binding.submitInForgotPassword.setOnClickListener {
            hitSendOtpLogin()
        }
        binding.forgotWithMobile.setOnClickListener {
            binding.enterEmailOrMobile.setText("")
            if (viewModel.emailVisibilityChk.value == true) {
                viewModel.emailVisibilityChk.postValue(false)
            } else if (viewModel.emailVisibilityChk.value == false) {
                viewModel.emailVisibilityChk.postValue(true)
            }
        }
    }

}