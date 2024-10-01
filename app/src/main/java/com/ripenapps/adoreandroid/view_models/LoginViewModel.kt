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
import com.ripenapps.adoreandroid.models.request_models.LoginRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.LoginResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import com.ripenapps.adoreandroid.views.fragments.LoginFragmentDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val application: Application) : ViewModel() {
    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val loginLiveData = SingleLiveEvent<Resources<LoginResponse>>()
    val logoutLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    val deleteAccountVerifyLiveData=SingleLiveEvent<Resources<LoginResponse>>()
    var locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()


    fun getDeleteAccountVerifyLiveData():LiveData<Resources<LoginResponse>>{
        return deleteAccountVerifyLiveData
    }
    fun getLogoutLiveData():LiveData<Resources<CommonResponse>>{
        return logoutLiveData
    }
    fun getLoginLiveData(): LiveData<Resources<LoginResponse>> {
        return loginLiveData
    }

    fun validateInput(): String? {
        val emailValue = email.get()?.trim()
        val passwordValue = password.get()?.trim()

        if (emailValue.isNullOrEmpty()) {
            return CommonUtils.getLocalisedString(R.string.please_enter_a_valid_email_or_user_id, locale, configuration, application)
        }

        if (!isValidEmail(emailValue)&&!isValidUsername(emailValue)) {
            return CommonUtils.getLocalisedString(R.string.please_enter_a_valid_email_or_user_id, locale, configuration, application)
        }

        if (passwordValue.isNullOrEmpty()) {
            return CommonUtils.getLocalisedString(R.string.please_enter_a_password, locale, configuration, application)
        }

        if (passwordValue.length < 8) {
            return CommonUtils.getLocalisedString(R.string.password_length_can_not_be_less, locale, configuration, application)
        }
//        if (!CommonUtils.isValidPassword(passwordValue)) {
//            return "Invalid password, it must be a combination of capital & small letters, numbers, and special characters."
//        }

        return null // No validation error
    }
    fun hitDeleteAccountVerifyApi(loginRequest: LoginRequest) {
        try {
            deleteAccountVerifyLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    deleteAccountVerifyLiveData.postValue(
                        Resources.success(
                            ApiRepository().deleteAccountVerifyApi(
                                loginRequest
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    deleteAccountVerifyLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    deleteAccountVerifyLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitUserLoginApi(loginRequest: LoginRequest) {
        try {
            loginLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    loginLiveData.postValue(
                        Resources.success(
                            ApiRepository().loginResponseApi(
                                loginRequest
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    loginLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    loginLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitUserLogoutApi(token:String) {
        try {
            logoutLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    logoutLiveData.postValue(
                        Resources.success(
                            ApiRepository().logoutApi(
                                "Bearer $token"
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    logoutLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    logoutLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun isValidUsername(input: String): Boolean {
        val regex = Regex("^[a-zA-Z]+[0-9_]*\$")
        return regex.matches(input)
    }


    fun onClick(view: View) {
        when (view.id) {
            R.id.signupFromWelcome -> {
                view.findNavController().navigate(LoginFragmentDirections.loginToSignUp())
            }

            R.id.forgotPasswordInLogin -> {
                view.findNavController().navigate(LoginFragmentDirections.loginToForgotPassword("login"))
            }

            R.id.loginWithMobileinLogin -> {
                view.findNavController().navigate(LoginFragmentDirections.loginToLoginWithMobile())
            }

            R.id.loginFromLogin -> {

            }
        }
    }
}