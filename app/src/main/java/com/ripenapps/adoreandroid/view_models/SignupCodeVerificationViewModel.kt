package com.ripenapps.adoreandroid.view_models

import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyOtpRequest
import com.ripenapps.adoreandroid.models.response_models.SendOtpResponse
import com.ripenapps.adoreandroid.models.response_models.VerifyOtpResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class SignupCodeVerificationViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {
    private lateinit var verifyOtpRequest: VerifyOtpRequest
    var verifyInSignUpCheck = savedStateHandle.get<String>("verifyInSignUpCheck")
    var navigateToQuestions = MutableLiveData(false)
    val otp = ObservableField<String>()
    private val otpVerificationLiveData = SingleLiveEvent<Resources<VerifyOtpResponse>>()
    private val resendOtpLiveData = SingleLiveEvent<Resources<SendOtpResponse>>()
    fun getOtpLiveData(): LiveData<Resources<VerifyOtpResponse>> {
        return otpVerificationLiveData
    }
    fun getResendLiveData():LiveData<Resources<SendOtpResponse>>{
        return resendOtpLiveData
    }
    fun hitResendOtpSignInApi(resendOtpRequest:SendOtpRequest){

        try {
            resendOtpLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    resendOtpLiveData.postValue(
                        Resources.success(
                            ApiRepository().reSendOtpApi(
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

    fun hitOtpVerificationApi() {
        Log.i("TAG", "hitOtpVerificationApi: "+otp.get())
        verifyOtpRequest = VerifyOtpRequest()
        if (verifyInSignUpCheck=="email"){
            verifyOtpRequest.countryCode = UserPreference.countryCode
            verifyOtpRequest.email = UserPreference.emailAddress
            verifyOtpRequest.otpEmail = otp.get()
        } else{
            verifyOtpRequest.countryCode = UserPreference.countryCode
            verifyOtpRequest.mobileOtp = otp.get()
            verifyOtpRequest.mobile = UserPreference.mobileNumber
        }

        try {
            otpVerificationLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    otpVerificationLiveData.postValue(
                        Resources.success(
                            ApiRepository().verifyOtpApi(
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
            R.id.backFromVerifyCodeSignUp -> {
                view.findNavController().popBackStack()
            }

        }
    }
}