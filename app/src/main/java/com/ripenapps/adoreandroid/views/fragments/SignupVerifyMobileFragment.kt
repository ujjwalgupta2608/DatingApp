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
import com.ripenapps.adoreandroid.databinding.FragmentSignupVerifyMobileBinding
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.SignUpViewModel
import com.ripenapps.adoreandroid.view_models.SignupVerifyMobileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupVerifyMobileFragment : BaseFragment<FragmentSignupVerifyMobileBinding>() {
    private val viewModelSignUpVerifyMobile by viewModels<SignupVerifyMobileViewModel>()
    private val viewModelSignUp by viewModels<SignUpViewModel>()

    val mobileNumber = MutableLiveData<String>()

    fun validateInput(): String? {
        if (mobileNumber.value.isNullOrEmpty()) {
            return getString(R.string.please_enter_your_mobile_number_to_proceed)
        }

        if (mobileNumber.value!!.length < 8) {
            return getString(R.string.invalid_mobile_number_number_format_please_enter_a_)
        }
        return null // No validation error
    }

    override fun setLayout(): Int {
        return R.layout.fragment_signup_verify_mobile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModelSignUpVerifyMobile
        initSignUpViewModel()
        binding.lifecycleOwner = this

        binding.enterEmailOrMobile.addTextChangedListener {
            // Update the mobileNumber LiveData on text change
            mobileNumber.value = it.toString()
        }

        viewModelSignUp.getSendOtpLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        "200" -> {
                            it.data?.message?.let { it1 ->
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            findNavController().navigate(
                                SignupVerifyMobileFragmentDirections.signupVerifyMobileToSignupCodeVerification(
                                    "mobile"
                                )
                            )
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

    private fun hitSendOtp() {
        val validationError = validateInput()
        if (validationError != null) {
            Snackbar.make(binding.emailOrMobileLayout, validationError, Snackbar.LENGTH_SHORT)
                .show()
        } else {
            var sendOtpRequest = SendOtpRequest()
            UserPreference.mobileNumber = mobileNumber.value!!
            UserPreference.countryCode = "+" + binding.countryCode.selectedCountryCode
            sendOtpRequest.mobile = mobileNumber.value!!
            sendOtpRequest.countryCode = "+" + binding.countryCode.selectedCountryCode
            viewModelSignUp.hitSendOtpApi(sendOtpRequest)
        }
    }

    private fun initSignUpViewModel() {

    }

}