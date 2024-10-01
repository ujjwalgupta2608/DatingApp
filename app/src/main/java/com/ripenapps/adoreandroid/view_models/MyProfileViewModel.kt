package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.WhatElseYouLikeResponse
import com.ripenapps.adoreandroid.models.response_models.boostData.BoostDataResponse
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestListResponse
import com.ripenapps.adoreandroid.models.response_models.getquestions.QuestionsDataResponse
import com.ripenapps.adoreandroid.models.response_models.updategallery.UpdateGalleryResponse
import com.ripenapps.adoreandroid.models.response_models.userprofiledetails.UserProfileDetailsResponse
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import com.ripenapps.adoreandroid.repository.ApiRepository
import com.ripenapps.adoreandroid.utils.CommonUtils
import com.ripenapps.adoreandroid.utils.Resources
import com.ripenapps.adoreandroid.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.IOException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private var userProfileDetailLiveData = SingleLiveEvent<Resources<UserProfileDetailsResponse>>()
    private var allListsLiveData = SingleLiveEvent<Resources<QuestionsDataResponse>>()
    private var updateUserProfileLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    private var interestListLiveData = SingleLiveEvent<Resources<InterestListResponse>>()
    private var updateProfileMediaLiveData = SingleLiveEvent<Resources<UpdateGalleryResponse>>()
    private var whatElseLikeLiveData = SingleLiveEvent<Resources<WhatElseYouLikeResponse>>()
    private var boostProfileLiveData = SingleLiveEvent<Resources<BoostDataResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getWhatElseLikeLiveData():LiveData<Resources<WhatElseYouLikeResponse>>{
        return whatElseLikeLiveData
    }
    fun getBoostProfileLiveData():LiveData<Resources<BoostDataResponse>>{
        return boostProfileLiveData
    }
    fun getUserProfileDetailLiveData(): LiveData<Resources<UserProfileDetailsResponse>> {
        return userProfileDetailLiveData
    }

    fun getAllListsLiveData(): LiveData<Resources<QuestionsDataResponse>> {
        return allListsLiveData
    }

    fun getUpdateProfileLiveData(): LiveData<Resources<CommonResponse>> {
        return updateUserProfileLiveData
    }

    fun getInterestListLiveData(): LiveData<Resources<InterestListResponse>> {
        return interestListLiveData
    }

    fun getUpdateGalleryMediaLiveData(): LiveData<Resources<UpdateGalleryResponse>> {
        return updateProfileMediaLiveData
    }

    fun hitBoostProfileApi(token: String) {
        try {
            boostProfileLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    boostProfileLiveData.postValue(
                        Resources.success(
                            ApiRepository().boostProfileApi("Bearer $token")
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    boostProfileLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitWhatElseLikeApi() {
        try {
            whatElseLikeLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    whatElseLikeLiveData.postValue(
                        Resources.success(
                            ApiRepository().whatElseYouLikeApi()
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    whatElseLikeLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitUpdateGalleryMediaApi(token:String, requestBody: MultipartBody) {
        try {
            updateProfileMediaLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    updateProfileMediaLiveData.postValue(
                        Resources.success(
                            ApiRepository().updateGalleryMediaApi(
                                "Bearer $token", requestBody
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    updateProfileMediaLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitGetInterestDataApi() {
        try {
            interestListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    interestListLiveData.postValue(Resources.success(ApiRepository().interestListApi()))
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    updateUserProfileLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitUpdateProfileApi(token: String, requestBody: MultipartBody) {
        try {
            updateUserProfileLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    updateUserProfileLiveData.postValue(
                        Resources.success(
                            ApiRepository().updateUserProfileApi(
                                "Bearer $token", requestBody
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    updateUserProfileLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitGetUserProfileDetailsApi(token: String) {
        try {
            userProfileDetailLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    userProfileDetailLiveData.postValue(
                        Resources.success(
                            ApiRepository().userProfileDetailsApi(
                                "Bearer $token"
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    userProfileDetailLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitGetAllListsDataApi(userId:String) {
        try {
            allListsLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    allListsLiveData.postValue(Resources.success(ApiRepository().updateUserDataApi(userId)))
                }
                catch (ex: IOException) {
                    ex.printStackTrace()
                    updateUserProfileLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                }catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}