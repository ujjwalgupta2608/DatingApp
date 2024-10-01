package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.ChangePasswordRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor( private val application: Application):ViewModel() {
    val currentPassword = ObservableField<String>()
    val newPassword = ObservableField<String>()
    val confirmPassword = ObservableField<String>()
    private val changePasswordLiveData=SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    private val configuration = Configuration()
    fun getChangePasswordLiveData():LiveData<Resources<CommonResponse>>{
        return changePasswordLiveData
    }

    fun hitChangePasswordApi(token:String, request: ChangePasswordRequest){
            try {
                changePasswordLiveData.postValue(Resources.loading(null))
                viewModelScope.launch {
                    try {
                        
                        changePasswordLiveData.postValue(
                            Resources.success(
                                ApiRepository().changePasswordApi(
                                    "Bearer $token", request
                                )
                            )
                        )
                    }catch (ex: IOException) {
                        ex.printStackTrace()
                        changePasswordLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                    } catch (ex: Exception) {
                        changePasswordLiveData.postValue(Resources.error(ex.localizedMessage, null))

                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
    }

    fun validateInput(): String? {
        if (currentPassword.get().isNullOrEmpty())
            return CommonUtils.getLocalisedString(R.string.please_enter_current_password, locale, configuration, application )
        if (newPassword.get().isNullOrEmpty())
            return CommonUtils.getLocalisedString(R.string.please_enter_new_password, locale, configuration, application)
        if (confirmPassword.get().isNullOrEmpty())
            return CommonUtils.getLocalisedString(R.string.please_enter_confirm_password, locale, configuration, application)
        if (newPassword.get()?.length!! < 8 && confirmPassword.get()?.length!! < 8){
            return CommonUtils.getLocalisedString(R.string.password_length_can_not_be_less, locale, configuration, application)
        }
        if (currentPassword.get() == newPassword.get()||currentPassword.get()==confirmPassword.get())
            return CommonUtils.getLocalisedString(R.string.current_password_and_new_password_must_be_different, locale, configuration, application)
        if (newPassword.get() != confirmPassword.get())
            return CommonUtils.getLocalisedString(R.string.new_password_and_confirm_password_must_be_same, locale, configuration, application)
        if (!CommonUtils.isValidPassword(newPassword.get()!!)||!CommonUtils.isValidPassword(newPassword.get()!!)) {
            return CommonUtils.getLocalisedString(R.string.invalid_password_it_must_be_a_combination_of_capital_small_letters_numbers_and_special_characters, locale, configuration, application)
        }
        return null
    }

    fun onClick(view:View){
        when(view.id){
            R.id.backFromChangePassword->{
                view.findNavController().popBackStack()
            }
            R.id.forgotPassword->{}
        }
    }
}
