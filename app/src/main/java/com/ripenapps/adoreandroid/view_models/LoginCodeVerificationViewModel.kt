package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyMobileLoginRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyOtpRequest
import com.ripenapps.adoreandroid.models.response_models.SendOtpResponse
import com.ripenapps.adoreandroid.models.response_models.VerifyMobileLoginResponse
import com.ripenapps.adoreandroid.models.response_models.VerifyOtpResponse
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginCodeVerificationViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {
    private lateinit var verifyOtpRequest: VerifyOtpRequest
    private lateinit var verifyMobileLoginRequest: VerifyMobileLoginRequest
    var emailNumberCheck = savedStateHandle.get<String>("emailNumberCheck")
    val otp = ObservableField<String>()
    private val otpVerificationLiveData = SingleLiveEvent<Resources<VerifyOtpResponse>>()
    private val resendOtpLiveData = SingleLiveEvent<Resources<SendOtpResponse>>()
    private val verifyMobileLoginLiveData = SingleLiveEvent<Resources<VerifyMobileLoginResponse>>()

    fun getOtpLiveData(): LiveData<Resources<VerifyOtpResponse>> {
        return otpVerificationLiveData
    }

    fun getResendLiveData(): LiveData<Resources<SendOtpResponse>> {
        return resendOtpLiveData
    }

    fun getVerifyMobileLoginLiveData():LiveData<Resources<VerifyMobileLoginResponse>>{
        return verifyMobileLoginLiveData
    }

    fun hitResendOtpLogInApi(resendOtpRequest: SendOtpRequest) {

        try {
            resendOtpLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    resendOtpLiveData.postValue(
                        Resources.success(
                            ApiRepository().resendOtpLoginApi(
                                resendOtpRequest
                            )
                        )
                    )
                } catch (ex: Exception) {
                    resendOtpLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitMobileLoginVerificationApi(verifyMobileLoginRequest: VerifyMobileLoginRequest){
        try {
            verifyMobileLoginLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    verifyMobileLoginLiveData.postValue(
                        Resources.success(
                            ApiRepository().verifyMobileLoginApi(
                                verifyMobileLoginRequest
                            )
                        )
                    )
                } catch (ex: Exception) {
                    verifyMobileLoginLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitOtpVerificationApi() {
        verifyOtpRequest = VerifyOtpRequest()
        if (emailNumberCheck == "forgotWithEmail") {
            verifyOtpRequest.email = UserPreference.emailAddress
            verifyOtpRequest.resetPasswordOTP = otp.get()
        } else if (emailNumberCheck == "forgotWithMobile") {
            verifyOtpRequest.resetPasswordOTP = otp.get()
            verifyOtpRequest.countryCode = UserPreference.countryCode
            verifyOtpRequest.mobile = UserPreference.mobileNumber
        }

        try {
            otpVerificationLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
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

    fun onClick(view: View) {
        when (view.id) {
            R.id.verifyCodeInLogin -> {
                when (emailNumberCheck.let { it }) {
                    "forgotWithEmail" -> {

                    }

                    "forgotWithMobile" -> {

                    }

                    "loginWithMobile" -> {
//                        will navigate to home
                    }
                }
            }

            R.id.backFromVerifyCodeLogin -> {
                view.findNavController().popBackStack()
            }
        }
    }
}