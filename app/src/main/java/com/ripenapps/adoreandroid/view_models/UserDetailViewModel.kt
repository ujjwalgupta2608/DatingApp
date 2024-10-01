package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.response_models.carddetails.CardDetailsResponse
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
class UserDetailViewModel @Inject constructor(private val application: Application):ViewModel() {
    private var userDetailLiveData=SingleLiveEvent<Resources<CardDetailsResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getUserDetailLiveData():LiveData<Resources<CardDetailsResponse>>{
        return userDetailLiveData
    }

    fun hitUserDetailApi(token:String, userId:String){
        try {
            userDetailLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    userDetailLiveData.postValue(
                        Resources.success(
                            ApiRepository().cardDetailsApi(
                                "Bearer $token", userId
                            )
                        )
                    )
                }
                catch (ex: IOException) {
                    ex.printStackTrace()
                    userDetailLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                }catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}