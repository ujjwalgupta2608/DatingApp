package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.SendMessageRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.helpList.HelpListResponse
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
class HelpViewModel @Inject constructor(val application: Application):ViewModel(){
    private var helpChatListLiveData=SingleLiveEvent<Resources<HelpListResponse>>()
    private var sendChatMessageLiveData=SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getHelpChatListLiveData():LiveData<Resources<HelpListResponse>>{
        return helpChatListLiveData
    }
    fun getSendMessageLiveData():LiveData<Resources<CommonResponse>>{
        return sendChatMessageLiveData
    }


    fun hitHelpChatListApi(token:String) {
        try {
            helpChatListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    helpChatListLiveData.postValue(
                        Resources.success(
                            ApiRepository().getHelpChatListApi(
                                "Bearer $token"
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    helpChatListLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    helpChatListLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitSendMessageApi(token:String, request:SendMessageRequest) {
        try {
            sendChatMessageLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    sendChatMessageLiveData.postValue(
                        Resources.success(
                            ApiRepository().sendHelpMessageApi(
                                "Bearer $token", request
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    sendChatMessageLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    sendChatMessageLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}