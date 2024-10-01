package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.UpdateLocationRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.UserSearchListResponse
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
class SearchViewModel @Inject constructor(private val application: Application):ViewModel() {
    private val searchUserLiveData=SingleLiveEvent<Resources<UserSearchListResponse>>()
    private val updateLocationLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getUserSearchListLiveData():LiveData<Resources<UserSearchListResponse>>{
        return searchUserLiveData
    }
    fun getUpdateLocationLiveData():LiveData<Resources<CommonResponse>>{
        return updateLocationLiveData
    }
    fun hitUpdateLocationApi(token: String, request:UpdateLocationRequest){
        try {
            updateLocationLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    updateLocationLiveData.postValue(
                        Resources.success(
                            ApiRepository().updateLocationApi(
                                "Bearer $token", request
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    updateLocationLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    updateLocationLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitUserSearchListApi(token: String, search:String?, filterCardListRequestKeys: FilterCardListRequestKeys, currentCity:String){
        try {
            searchUserLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    searchUserLiveData.postValue(
                        Resources.success(
                            ApiRepository().userSearchListApi(
                                "Bearer $token",
                                search,
                                if(!filterCardListRequestKeys.city.isNullOrEmpty()) /*(if(!currentCity.isNullOrEmpty()) currentCity else */filterCardListRequestKeys.city/*)*/ else currentCity,
                                if(!currentCity.isNullOrEmpty())  0 else filterCardListRequestKeys.minAge,
                                if(!currentCity.isNullOrEmpty())  0 else filterCardListRequestKeys.maxAge,
                                if(!currentCity.isNullOrEmpty())  -1 else (if (!filterCardListRequestKeys.city.isNullOrEmpty()) 0 else filterCardListRequestKeys.minDistance),
                                if(!currentCity.isNullOrEmpty())  0 else (if (!filterCardListRequestKeys.city.isNullOrEmpty()) 0 else filterCardListRequestKeys.maxDistance),
                                if(!currentCity.isNullOrEmpty())  "" else filterCardListRequestKeys.sortBy,
                                if(!currentCity.isNullOrEmpty())  "" else filterCardListRequestKeys.interestedIn

                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    searchUserLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    searchUserLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}