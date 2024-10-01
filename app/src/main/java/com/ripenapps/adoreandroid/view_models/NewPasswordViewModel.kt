package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.ResetPasswordRequest
import com.ripenapps.adoreandroid.models.response_models.ResetPasswordResponse
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.google.android.material.snackbar.Snackbar
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject


@HiltViewModel
class NewPasswordViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle, private val application: Application) : ViewModel() {
    var otp = savedStateHandle.get<String>("otp")
    var emailNumberCheck=savedStateHandle.get<String>("emailNumberCheck")
    val newPassword = ObservableField<String>()
    val confirmPassword = ObservableField<String>()
    var resetPasswordLiveData = SingleLiveEvent<Resources<ResetPasswordResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getResetPasswordLiveData():LiveData<Resources<ResetPasswordResponse>>{
        return resetPasswordLiveData
    }
    fun validateInput(): String? {
        if (newPassword.get().isNullOrEmpty())
            return CommonUtils.getLocalisedString(R.string.please_enter_new_password, locale, configuration, application)
        if (confirmPassword.get().isNullOrEmpty())
            return CommonUtils.getLocalisedString(R.string.please_enter_confirm_password, locale, configuration, application)
        if (newPassword.get()?.length!! < 8 && confirmPassword.get()?.length!! < 8){
            return CommonUtils.getLocalisedString(R.string.password_length_can_not_be_less, locale, configuration, application)
        }

        if (newPassword.get() != confirmPassword.get())
            return CommonUtils.getLocalisedString(R.string.new_password_and_confirm_password_must_be_same, locale, configuration, application)
        if (!CommonUtils.isValidPassword(newPassword.get()!!)||!CommonUtils.isValidPassword(newPassword.get()!!)) {
            return CommonUtils.getLocalisedString(R.string.invalid_password_it_must_be_a_combination_of_capital_small_letters_numbers_and_special_characters, locale, configuration, application)
        }
        return null
    }
    fun hitResetPasswordApi(request: ResetPasswordRequest){
        try {
            resetPasswordLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    resetPasswordLiveData.postValue(
                        Resources.success(
                            ApiRepository().resetPasswordResponseApi(
                                request
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    resetPasswordLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    resetPasswordLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.backFromNewPassword -> {
                view.findNavController().popBackStack()
            }

            R.id.submitInConfirmPassword -> {
                val validationError = validateInput()

                if (validationError != null) {
                    Snackbar.make(view, validationError, Snackbar.LENGTH_SHORT).show()
                } else {
                    var resetPasswordRequest = ResetPasswordRequest()
                    if (emailNumberCheck=="forgotWithEmail"){
                        resetPasswordRequest.email = UserPreference.emailAddress
                    }else{
                        resetPasswordRequest.countryCode = UserPreference.countryCode
                        resetPasswordRequest.mobile = UserPreference.mobileNumber
                    }
                    resetPasswordRequest.resetPasswordOTP = otp
                    resetPasswordRequest.newPassword = newPassword.get()
                    resetPasswordRequest.confirmPassword = confirmPassword.get()
                    hitResetPasswordApi(request = resetPasswordRequest)
                }
            }
        }
    }
}