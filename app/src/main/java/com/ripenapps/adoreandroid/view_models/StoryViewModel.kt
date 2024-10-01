package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.myStoryList.MyStoryResponse
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListingResponse
import com.ripenapps.adoreandroid.models.response_models.storyViewers.StoryViewersResponse
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
class StoryViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val userListingLiveData = SingleLiveEvent<Resources<StoryListingResponse>>()
    private val myStoryListingLiveData = SingleLiveEvent<Resources<MyStoryResponse>>()
    private val storyViewersListLiveData = SingleLiveEvent<Resources<StoryViewersResponse>>()
    private val otherStoryListingLiveData = SingleLiveEvent<Resources<MyStoryResponse>>()
    private val deleteStoryLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    private val viewStoryLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getUserListingLiveData(): LiveData<Resources<StoryListingResponse>> {
        return userListingLiveData
    }

    fun myStoryListingLiveData(): LiveData<Resources<MyStoryResponse>> {
        return myStoryListingLiveData
    }

    fun getStoryViewersLiveData(): LiveData<Resources<StoryViewersResponse>> {
        return storyViewersListLiveData
    }

    fun getOtherStoryListingLiveData(): LiveData<Resources<MyStoryResponse>> {
        return otherStoryListingLiveData
    }

    fun getDeleteStoryLiveData(): LiveData<Resources<CommonResponse>> {
        return deleteStoryLiveData
    }

    fun getViewStoryLiveData(): LiveData<Resources<CommonResponse>> {
        return viewStoryLiveData
    }

    fun hitDeleteStoryApi(token: String, userId: String) {
        try {
            deleteStoryLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    deleteStoryLiveData.postValue(
                        Resources.success(
                            ApiRepository().deleteStoryApi(
                                "Bearer $token", userId
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    deleteStoryLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    deleteStoryLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitMyStoryListingApi(token: String) {
        try {
            myStoryListingLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    myStoryListingLiveData.postValue(
                        Resources.success(
                            ApiRepository().myStoryListApi(
                                "Bearer $token"
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    myStoryListingLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    myStoryListingLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitOtherStoryListingApi(token: String, userId: String) {
        try {
            otherStoryListingLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    otherStoryListingLiveData.postValue(
                        Resources.success(
                            ApiRepository().otherStoryList(
                                "Bearer $token", userId
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    otherStoryListingLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    otherStoryListingLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitStoryViewersListApi(token: String, userId: String) {
        try {
            storyViewersListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    storyViewersListLiveData.postValue(
                        Resources.success(
                            ApiRepository().storyViewersListApi(
                                "Bearer $token", userId
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    storyViewersListLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    storyViewersListLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitUserListingApi(token: String) {
        try {
            userListingLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    userListingLiveData.postValue(
                        Resources.success(
                            ApiRepository().storyListingApi(
                                "Bearer $token"
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    userListingLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    userListingLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun hitViewStoryApi(token: String, storyId: String) {
        try {
            viewStoryLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    viewStoryLiveData.postValue(
                        Resources.success(
                            ApiRepository().viewStoryApi(
                                "Bearer $token", storyId
                            )
                        )
                    )
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    viewStoryLiveData.postValue(
                        Resources.error(
                            CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application),
                            null
                        )
                    )

                } catch (ex: Exception) {
                    viewStoryLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}