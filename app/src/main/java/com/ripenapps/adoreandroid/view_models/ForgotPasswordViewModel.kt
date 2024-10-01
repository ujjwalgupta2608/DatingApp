package com.ripenapps.adoreandroid.view_models

import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.response_models.SendOtpResponse
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor() : ViewModel() {
    var emailVisibilityChk = MutableLiveData(true)
    var sendOtpLiveData = SingleLiveEvent<Resources<SendOtpResponse>>()
    val mobileNumber = ObservableField<String>()

    fun getSendOtpLiveData(): LiveData<Resources<SendOtpResponse>> {
        return sendOtpLiveData
    }

    fun hitSendOtpLoginApi(sendOtpRequest: SendOtpRequest) {
        try {
            sendOtpLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    sendOtpLiveData.postValue(
                        Resources.success(
                            ApiRepository().sendOtpLoginApi(
                                sendOtpRequest
                            )
                        )
                    )
                } catch (ex: Exception) {
                    sendOtpLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.backFromForgotPassword -> {
                view.findNavController().popBackStack()
            }

            R.id.forgotWithMobile -> {
//                if (emailVisibilityChk.value == true) {
//                    emailVisibilityChk.postValue(false)
//                } else if (emailVisibilityChk.value == false) {
//                    emailVisibilityChk.postValue(true)
//                }
            }

            R.id.submitInForgotPassword -> {

//                if (emailVisibilityChk.value==true){
//                    view.findNavController().navigate(ForgotPasswordFragmentDirections.forgotPasswordToLoginCodeVerification("forgotWithEmail"))
//                }
//                else{
//                    view.findNavController().navigate(ForgotPasswordFragmentDirections.forgotPasswordToLoginCodeVerification("forgotWithMobile"))
//                }
            }
        }
    }

}