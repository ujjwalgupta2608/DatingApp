package com.ripenapps.adoreandroid.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentLoginWithMobileBinding
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.ForgotPasswordViewModel
import com.ripenapps.adoreandroid.view_models.LoginWithMobileViewModel

class LoginWithMobileFragment : BaseFragment<FragmentLoginWithMobileBinding>() {
    val viewModel by viewModels<LoginWithMobileViewModel>()
    val forgotPasswordViewModel by viewModels<ForgotPasswordViewModel>()
    val mobileNumber = MutableLiveData<String>()
    var previousScreen = ""

    override fun setLayout(): Int {
        return R.layout.fragment_login_with_mobile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        binding.enterEmailOrMobile.addTextChangedListener {
            // Update the mobileNumber LiveData on text change
            mobileNumber.value = it.toString()
        }
        forgotPasswordViewModel.getSendOtpLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        "200" -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(requireActivity(), it1, Toast.LENGTH_SHORT).show()
                            }
                            findNavController().navigate(
                                LoginWithMobileFragmentDirections.loginWithMobileToLoginCodeVerification(
                                    "loginWithMobile" ,"login"
                                )
                            )

                        } else->{
                        it.data?.message?.let { it1 ->
                            Toast.makeText(requireActivity(), it1, Toast.LENGTH_SHORT).show()
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
        binding.clickEvents = ::onClick

    }

    fun onClick(value: Int) {
        when (value) {
            0 -> {
                hitSendOtp()
            }
        }
    }

    fun hitSendOtp() {
        val validationError = validateInput()
        if (validationError != null) {
            Toast.makeText(requireActivity(), validationError, Toast.LENGTH_SHORT).show()
        } else {
            var sendOtpRequest = SendOtpRequest()
            UserPreference.mobileNumber = mobileNumber.value!!
            UserPreference.countryCode = "+"+binding.countryCode.selectedCountryCode
            sendOtpRequest.mobile = mobileNumber.value!!
            sendOtpRequest.countryCode = "+"+binding.countryCode.selectedCountryCode
            forgotPasswordViewModel.hitSendOtpLoginApi(sendOtpRequest)
        }
    }

    fun validateInput(): String? {
        if (mobileNumber.value.isNullOrEmpty()) {
            return getString(R.string.please_enter_your_mobile_number_to_proceed)
        }

        if (mobileNumber.value!!.length < 8) {
            return getString(R.string.invalid_mobile_number_number_format_please_enter_a_)
        }
        return null // No validation error
    }
}