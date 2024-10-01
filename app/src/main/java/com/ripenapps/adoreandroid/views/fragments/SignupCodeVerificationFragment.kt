package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentSignupCodeVerificationBinding
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.SignupCodeVerificationViewModel
import com.ripenapps.adoreandroid.views.activities.QuestionsActivity
import com.google.android.material.snackbar.Snackbar

class SignupCodeVerificationFragment : BaseFragment<FragmentSignupCodeVerificationBinding>() {
    lateinit var viewModel: SignupCodeVerificationViewModel
    private lateinit var resendOtpRequest: SendOtpRequest
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private val timerDuration = 45 * 1000
    var emailOrNumber = ""

    override fun setLayout(): Int {
        return R.layout.fragment_signup_code_verification
    }

    fun onClick() {
        binding.verifyCodeInSignup.setOnClickListener {
            if (viewModel.verifyInSignUpCheck?.let { it == "email" } == true)
                if (viewModel.otp.get().isNullOrEmpty()) {
                    Snackbar.make(requireView(), getString(R.string.please_enter_a_valid_otp), Snackbar.LENGTH_SHORT)
                        .show()
                } else if (viewModel.otp.get()?.length!! < 4) {
                    Snackbar.make(requireView(), getString(R.string.please_enter_a_valid_otp), Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.hitOtpVerificationApi()

                }
            else {
                if (viewModel.otp.get().isNullOrEmpty()) {
                    Snackbar.make(requireView(), getString(R.string.please_enter_a_valid_otp), Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    viewModel.hitOtpVerificationApi()

                }
            }
        }
        binding.resendOtp.setOnClickListener {
            resendOtpRequest = SendOtpRequest()
            if (viewModel.verifyInSignUpCheck == "email") {
                resendOtpRequest.email = UserPreference.emailAddress
            } else {
                resendOtpRequest.countryCode = UserPreference.countryCode
                resendOtpRequest.mobile = UserPreference.mobileNumber
            }
            viewModel.hitResendOtpSignInApi(resendOtpRequest)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SignupCodeVerificationViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        if (!isTimerRunning) {
            startResendTimer()
        }
        binding.resendOtp.paintFlags = binding.resendOtp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        if (viewModel.verifyInSignUpCheck == "email")
            emailOrNumber =
                "<font color=#242424>${getString(R.string.please_enter_the_code_we_just_sent_to)}</font> <font color=#6D53F4>${
                    UserPreference.emailAddress
                }</font>"
        else
            emailOrNumber =
                "<font color=#242424>${getString(R.string.please_enter_the_code_we_just_sent_to)}</font> <font color=#6D53F4>${UserPreference.countryCode}${" "}${
                    UserPreference.mobileNumber
                }</font>"
        binding.emailOrNumberText.text = Html.fromHtml(emailOrNumber, Html.FROM_HTML_MODE_LEGACY)
        viewModel.getOtpLiveData().observe(viewLifecycleOwner) {
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
                            if (viewModel.verifyInSignUpCheck == "email") {
                                findNavController().navigate(
                                    SignupCodeVerificationFragmentDirections.verifyCodeSignupToSignupVerifyMobile()
                                )
                            } else {
                                viewModel.navigateToQuestions.value = true
                                val intent =
                                    Intent(requireActivity(), QuestionsActivity::class.java)
                                startActivity(intent)
                            }
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
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }

        viewModel.getResendLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.status) {
                        "200" -> {
                            Toast.makeText(requireActivity(), it.data?.message, Toast.LENGTH_SHORT)
                                .show()
                            if (!isTimerRunning) {
                                startResendTimer()
                            }
                        }

                        else -> {
                            Toast.makeText(requireActivity(), it.data?.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    ProcessDialog.dismissDialog(true)
                }

                Status.LOADING -> {
                    ProcessDialog.showDialog(requireActivity(), true)                }

                Status.ERROR -> {
                    ProcessDialog.dismissDialog(true)
                    Log.e("TAG", "initViewModel: ${it.message}")
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        onClick()
    }

    private fun startResendTimer() {
        isTimerRunning = true
        timer = object : CountDownTimer(timerDuration.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val timerText = "${getString(R.string.resend_in)} $secondsRemaining ${getString(R.string.seconds)}"
                binding.resendOtp.text = timerText
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.resendOtp.text = getString(R.string.resend_otp)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the timer to prevent memory leaks
        timer?.cancel()
        timer = null
    }

}