package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.response_models.connectionlist.ConnectionListResponse
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
class LikeViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val connectionLiveData= SingleLiveEvent<Resources<ConnectionListResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getConnectionListLiveData():LiveData<Resources<ConnectionListResponse>>{
        return connectionLiveData
    }
     fun hitConnectionListApi(token:String, status:String){
         try {
             connectionLiveData.postValue(Resources.loading(null))
             viewModelScope.launch {
                 try {
                     connectionLiveData.postValue(
                         Resources.success(
                             ApiRepository().connectionListApi(
                                 "Bearer $token", status
                             )
                         )
                     )
                 }
                 catch (ex: IOException) {
                     ex.printStackTrace()
                     connectionLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                 }catch (ex: Exception) {
                     ex.printStackTrace()
                 }
             }

         } catch (ex: Exception) {
             ex.printStackTrace()
         }
     }

}