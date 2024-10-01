package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.SocialLoginRequest
import com.ripenapps.adoreandroid.models.response_models.SendOtpResponse
import com.ripenapps.adoreandroid.models.response_models.carddetails.CardDetailsResponse
import com.ripenapps.adoreandroid.preferences.UserPreference
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.views.fragments.SignUpFragmentDirections
import com.google.android.material.snackbar.Snackbar
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(private val application: Application) : ViewModel() {
    var agreeTermsConditions = MutableLiveData(false)
    val name = ObservableField<String>()
    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val sendOtpLiveData = SingleLiveEvent<Resources<SendOtpResponse>>()
    val socialLoginLiveData = SingleLiveEvent<Resources<CardDetailsResponse>>()
    var locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getSocialLoginLiveData():LiveData<Resources<CardDetailsResponse>>{
        return socialLoginLiveData
    }
    fun getSendOtpLiveData(): LiveData<Resources<SendOtpResponse>> {
        return sendOtpLiveData
    }

    fun hitSocialLoginLiveData(request:SocialLoginRequest){
        try {
            socialLoginLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    socialLoginLiveData.postValue(
                        Resources.success(
                            ApiRepository().socialLoginApi(
                                request
                            )
                        )
                    )
                }
                catch (ex: IOException) {
                    ex.printStackTrace()
                    socialLoginLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                }catch (ex: Exception) {
                    socialLoginLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitSendOtpApi(sendOtpRequest: SendOtpRequest) {
        try {
            sendOtpLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    sendOtpLiveData.postValue(
                        Resources.success(
                            ApiRepository().sendOtpSignupApi(
                                sendOtpRequest
                            )
                        )
                    )
                }
                catch (ex: IOException) {
                    ex.printStackTrace()
                    sendOtpLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                }catch (ex: Exception) {
                    sendOtpLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun validateInput(): String? {
        val nameValue = name.get()?.trim()
        val emailValue = email.get()?.trim()
        val passwordValue = password.get()  ?.trim()

        if (nameValue.isNullOrEmpty()&&emailValue.isNullOrEmpty()&&passwordValue.isNullOrEmpty()){
            return CommonUtils.getLocalisedString(R.string.please_fill_out_all_the_required_fields, locale, configuration, application)
        }

        if (nameValue.isNullOrEmpty()) {
            return CommonUtils.getLocalisedString(R.string.name_cannot_be_empty, locale, configuration, application)
        }

        /*if (!isValidName(nameValue)) {
            return CommonUtils.getLocalisedString(R.string.please_enter_a_valid_name_in_alphabets, locale, configuration, application)
        }*/

        if (emailValue.isNullOrEmpty()) {
            return CommonUtils.getLocalisedString(R.string.please_enter_a_valid_email_to_proceed, locale, configuration, application)
        }

        if (!isValidEmail(emailValue)) {
            return CommonUtils.getLocalisedString(R.string.invalid_email_format_please_enter_a_valid_email_to_proceed, locale, configuration, application)
        }

        if (passwordValue.isNullOrEmpty()) {
            return CommonUtils.getLocalisedString(R.string.please_enter_new_password, locale, configuration, application)
        }
        if (passwordValue.length<8){
            return CommonUtils.getLocalisedString(R.string.password_length_can_not_be_less, locale, configuration, application)
        }
        if (!CommonUtils.isValidPassword(passwordValue)) {
            return CommonUtils.getLocalisedString(R.string.invalid_password_it_must_be_a_combination_of_capital_small_letters_numbers_and_special_characters, locale, configuration, application)
        }
        if (agreeTermsConditions.value == false) {
            return CommonUtils.getLocalisedString(R.string.please_accept_the_terms_and_conditions_to_proceed, locale, configuration, application)
        }

        return null // No validation error
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
    private fun isValidName(name: String): Boolean {
        val namePattern = "^\\p{L}+|\\p{IsDevanagari}+|\\p{IsArabic}+\$"
        return name.matches(namePattern.toRegex())
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.termsConditionsButton -> {
                if (agreeTermsConditions.value == true) {
                    agreeTermsConditions.postValue(false)
                } else if (agreeTermsConditions.value == false) {
                    agreeTermsConditions.postValue(true)
                }
            }

            R.id.signupInSignup -> {
                val validationError = validateInput()

                if (validationError != null) {
                    Snackbar.make(view, validationError, Snackbar.LENGTH_SHORT).show()
                } else {
                    var sendOtpRequest = SendOtpRequest()
                    UserPreference.emailAddress = email.get()!!.trim()
                    if (name.get()!!.trim().length==1){
                        UserPreference.fullName=name.get()!!.trim().toUpperCase()
                    }else{
                        UserPreference.fullName = name.get()!!.trim().substring(0,1).toUpperCase() + name.get()!!.trim().substring(1)
                    }
                    Log.i("TAG", "onClick: "+UserPreference.fullName)
                    sendOtpRequest.email = email.get()!!.trim()
                    hitSendOtpApi(sendOtpRequest)
                }
            }

            R.id.loginFromSignup -> {
                view.findNavController().navigate(SignUpFragmentDirections.signUpToLogin())
            }
        }
    }
}