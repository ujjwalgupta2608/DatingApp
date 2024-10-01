package com.ripenapps.adoreandroid.views.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FragmentLoginCodeVerificationBinding
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyMobileLoginRequest
import com.ripenapps.adoreandroid.preferences.FCM_TOKEN
import com.ripenapps.adoreandroid.preferences.IS_LOGIN
import com.ripenapps.adoreandroid.preferences.NAME
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.TOKEN
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.preferences.USER_NAME
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.utils.BaseFragment
import com.ripenapps.adoreandroid.utils.ProcessDialog
import com.ripenapps.adoreandroid.utils.Status
import com.ripenapps.adoreandroid.view_models.ForgotPasswordViewModel
import com.ripenapps.adoreandroid.view_models.LoginCodeVerificationViewModel
import com.ripenapps.adoreandroid.views.activities.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.ripenapps.adoreandroid.preferences.EMAIL

class LoginCodeVerificationFragment : BaseFragment<FragmentLoginCodeVerificationBinding>() {
    private lateinit var verifyMobileLoginRequest: VerifyMobileLoginRequest
    val viewModel by viewModels<LoginCodeVerificationViewModel>()
    val forgotPasswordViewModel by viewModels<ForgotPasswordViewModel>()
    var previousScreen = ""

    var emailOrNumber = ""
    private lateinit var resendOtpRequest: SendOtpRequest
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private val timerDuration = 45 * 1000
    override fun setLayout(): Int {
        return R.layout.fragment_login_code_verification
    }

    fun onClick() {
        binding.verifyCodeInLogin.setOnClickListener {
            if (viewModel.otp.get().isNullOrEmpty()) {
                Snackbar.make(requireView(), "${getString(R.string.please_enter_a_valid_otp)}", Snackbar.LENGTH_SHORT)
                    .show()
            } else if (viewModel.otp.get()?.length!! < 4) {
                Snackbar.make(requireView(), "${getString(R.string.please_enter_a_valid_otp)}", Snackbar.LENGTH_SHORT)
                    .show()
            } else if (viewModel.emailNumberCheck == "forgotWithEmail" || viewModel.emailNumberCheck == "forgotWithMobile") {
                viewModel.hitOtpVerificationApi()
            } else if (viewModel.emailNumberCheck == "loginWithMobile") {
                verifyMobileLoginRequest = VerifyMobileLoginRequest()
                verifyMobileLoginRequest.mobile = UserPreference.mobileNumber
                verifyMobileLoginRequest.countryCode = UserPreference.countryCode
                verifyMobileLoginRequest.resetPasswordOTP = viewModel.otp.get()
                verifyMobileLoginRequest.deviceType = "android"
                verifyMobileLoginRequest.deviceToken = Preferences.getStringPreference(requireContext(), FCM_TOKEN).toString()
                viewModel.hitMobileLoginVerificationApi(verifyMobileLoginRequest)
            }
        }
        binding.resendOtp.setOnClickListener {
            resendOtpRequest = SendOtpRequest()
            if (viewModel.emailNumberCheck == "forgotWithEmail") {
                resendOtpRequest.email = UserPreference.emailAddress
                viewModel.hitResendOtpLogInApi(resendOtpRequest)
            } else if (viewModel.emailNumberCheck == "forgotWithMobile") {
                resendOtpRequest.countryCode = UserPreference.countryCode
                resendOtpRequest.mobile = UserPreference.mobileNumber
                viewModel.hitResendOtpLogInApi(resendOtpRequest)
            } else if (viewModel.emailNumberCheck == "loginWithMobile") {
                var sendOtpRequest = SendOtpRequest()
                sendOtpRequest.mobile = UserPreference.mobileNumber
                sendOtpRequest.countryCode = UserPreference.countryCode
                forgotPasswordViewModel.hitSendOtpLoginApi(sendOtpRequest)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.resendOtp.paintFlags = binding.resendOtp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        if (viewModel.emailNumberCheck.equals("forgotWithEmail"))
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
        if (!isTimerRunning) {
            startResendTimer()
        }
        viewModel.getOtpLiveData().observe(viewLifecycleOwner) {
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
                                if (viewModel.emailNumberCheck == "forgotWithEmail" || viewModel.emailNumberCheck == "forgotWithMobile") {
                                    findNavController().navigate(
                                        LoginCodeVerificationFragmentDirections.loginCodeVerificationToNewPassword(
                                            viewModel.otp.get()!!, previousScreen, viewModel.emailNumberCheck!!
                                        )
                                    )
                                } else {

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
            forgotPasswordViewModel.getSendOtpLiveData().observe(viewLifecycleOwner) {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.message.let { it1 ->
                            when (it.data?.status) {
                                "200" -> {
                                    Toast.makeText(
                                        requireActivity(),
                                        it1,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (!isTimerRunning) {
                                        startResendTimer()
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
                        ProcessDialog.dismissDialog(true)
                    }

                }
            }

        }

        viewModel.getResendLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (!isTimerRunning) {
                                    startResendTimer()
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
                    ProcessDialog.dismissDialog(true)
                }
            }
        }

        viewModel.getVerifyMobileLoginLiveData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.message.let { it1 ->
                        when (it.data?.status) {
                            "200" -> {
                                Toast.makeText(
                                    requireActivity(),
                                    it1,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Preferences.setStringPreference(requireActivity(), TOKEN, it.data.data?.token)
                                Preferences.setStringPreference(requireActivity(), NAME, it.data.data?.user?.name)
                                Preferences.setStringPreference(requireActivity(), USER_NAME, it.data.data?.user?.userName)
                                Preferences.setStringPreference(requireActivity(), IS_LOGIN, "true")
                                Preferences.setStringPreference(requireActivity(), USER_ID, it.data.data?.user?._id)
                                Preferences.setStringPreference(requireActivity(), EMAIL, it.data.data?.user?.email)
                                UserPreference.isNewUserRegistered=true
                                val intent = Intent(activity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                activity?.finish()
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
    }

    override fun initView(savedInstanceState: Bundle?) {
        previousScreen = LoginCodeVerificationFragmentArgs.fromBundle(requireArguments()).previousScreen
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