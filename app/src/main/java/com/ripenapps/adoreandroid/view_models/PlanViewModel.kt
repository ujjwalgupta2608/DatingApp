package com.ripenapps.adoreandroid.view_models

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.models.request_models.PlanCancelRequest
import com.ripenapps.adoreandroid.models.request_models.PlanPurchaseRequest
import com.ripenapps.adoreandroid.models.request_models.PlanRenewModel
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.planList.PlanListResponse
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
class PlanViewModel @Inject constructor(private val application: Application):ViewModel() {
    private val planListLiveData= SingleLiveEvent<Resources<PlanListResponse>>()
    private val planPurchaseLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    private val planRenewLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    private val planCancelLiveData = SingleLiveEvent<Resources<CommonResponse>>()
    val locale = Locale(Preferences.getStringPreference(application, SELECTED_LANGUAGE_CODE))
    val configuration = Configuration()
    fun getPlanListLiveData(): LiveData<Resources<PlanListResponse>> {
        return planListLiveData
    }
    fun getPlanPurchaseLiveData():LiveData<Resources<CommonResponse>>{
        return planPurchaseLiveData
    }
    fun getPlanRenewLiveData():LiveData<Resources<CommonResponse>>{
        return planRenewLiveData
    }
    fun getPlanCancelLiveData() : SingleLiveEvent<Resources<CommonResponse>>{
        return planCancelLiveData
    }
    fun hitPlanCancelApi(token:String, request:PlanCancelRequest){
        try {
            planCancelLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    planCancelLiveData.postValue(
                        Resources.success(
                            ApiRepository().planCancelApi(
                                "Bearer $token",
                                request
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    planCancelLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    planCancelLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitPlanRenewApi(token:String, request: PlanRenewModel){
        try {
            planRenewLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    planRenewLiveData.postValue(
                        Resources.success(
                            ApiRepository().planRenewApi(
                                "Bearer $token",
                                request
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    planRenewLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    planRenewLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitPlanPurchaseApi(token:String, request:PlanPurchaseRequest){
        try {
            planPurchaseLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    planPurchaseLiveData.postValue(
                        Resources.success(
                            ApiRepository().planPurchaseApi(
                                "Bearer $token",
                                request
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    planPurchaseLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    planPurchaseLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun hitPlanListApi(token:String){

        try {
            planListLiveData.postValue(Resources.loading(null))
            viewModelScope.launch {
                try {
                    planListLiveData.postValue(
                        Resources.success(
                            ApiRepository().getPlanApi(
                                "Bearer $token"
                            )
                        )
                    )
                }catch (ex: IOException) {
                    ex.printStackTrace()
                    planListLiveData.postValue(Resources.error(CommonUtils.getLocalisedString(R.string.unable_to_connect_please_check_your_internet, locale, configuration, application), null))

                } catch (ex: Exception) {
                    planListLiveData.postValue(Resources.error(ex.localizedMessage, null))

                }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}