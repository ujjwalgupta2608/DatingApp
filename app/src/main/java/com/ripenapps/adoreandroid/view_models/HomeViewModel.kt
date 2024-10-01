package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.FilterCardListRequestKeys
import com.ripenapps.adoreandroid.models.response_models.cardlist.CardListResponse
import com.ripenapps.adoreandroid.models.response_models.likeUnlikeResponse.LikeUnlikeResponse
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationResponse
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
class HomeViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val uploadStoryLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    private val cardListLiveData = SingleLiveEvent<Resources<CardListResponse>>()
    private val likeDislikeLiveData = SingleLiveEvent<Resources<LikeUnlikeResponse>>()
    private var notificationListLiveData = SingleLiveEvent<Resources<NotificationResponse>>()
    private val deleteNotificationLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()

    fun getUploadStoryLiveData(): LiveData<Resources<CommonResponse>> {
        return uploadStoryLiveData
    }

    fun getCardListApi(): LiveData<Resources<CardListResponse>> {
        return cardListLiveData
    }

    fun getLikeDislikeLiveData(): LiveData<Resources<LikeUnlikeResponse>> {
        return likeDislikeLiveData
    }

    fun getNotificationListLiveData(): LiveData<Resources<NotificationResponse>> {
        return notificationListLiveData
    }

    fun getDeleteNotificationLiveData(): LiveData<Resources<CommonResponse>> {
        return deleteNotificationLiveData
    }

    fun hitLikeDislikeUserApi(token: String, request: LikeDislikeRequest, userId: String) {
        try {
            likeDislikeLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    likeDislikeLiveData.postValue(
                        Resources.success(
                            ApiRepository().likeDislikeUserApi(
                                "Bearer $token", request, userId
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    likeDislikeLiveData.postValue(
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

    fun hitCardListApi(
        token: String,
        page: Int,
        limit: Int,
        filterCardListRequestKeys: FilterCardListRequestKeys
    ) {
        try {
            cardListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    cardListLiveData.postValue(
                        Resources.success(
                            ApiRepository().cardListApi(
                                "Bearer $token",
                                page,
                                limit,
                                filterCardListRequestKeys.city,
                                filterCardListRequestKeys.minAge,
                                filterCardListRequestKeys.maxAge,
                                filterCardListRequestKeys.minDistance,
                                filterCardListRequestKeys.maxDistance,
                                filterCardListRequestKeys.relationshipGoal,
                                filterCardListRequestKeys.complexion,
                                filterCardListRequestKeys.interestedIn,
                                filterCardListRequestKeys.sexualOrientation,
                                filterCardListRequestKeys.sortBy,
                                filterCardListRequestKeys.whatElseYouLike
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    cardListLiveData.postValue(
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

    fun hitUploadStoryLiveDataApi(token: String, requestBody: MultipartBody) {
        try {
            uploadStoryLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    uploadStoryLiveData.postValue(
                        Resources.success(
                            ApiRepository().uploadStoryApi(
                                "Bearer $token", requestBody
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    uploadStoryLiveData.postValue(
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

    fun hitNotificationListApi(
        token: String,
        type: String,
        page: String,
        limit: String,
        senderId: String
    ) {
        try {
            notificationListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    notificationListLiveData.postValue(
                        Resources.success(
                            ApiRepository().notificationListApi(
                                "Bearer $token", type, page, limit, senderId
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    notificationListLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    notificationListLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitDeleteNotificationApi(
        token: String,
        notificationId: String,
        status: String,
        senderId: String,
        roomId:String,
        chatId:String
    ) {
        try {
            deleteNotificationLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    deleteNotificationLiveData.postValue(
                        Resources.success(
                            ApiRepository().deleteNotificationApi(
                                "Bearer $token", notificationId, status, senderId, roomId, chatId
                            )
                        )
                    )

                } catch (ex: IOException) {
                    ex.printStackTrace()
                    deleteNotificationLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    deleteNotificationLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}