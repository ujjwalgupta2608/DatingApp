package com.ripenapps.adoreandroid.repository

import com.ripenapps.adoreandroid.models.request_models.ChangePasswordRequest
import com.ripenapps.adoreandroid.models.request_models.CheckVersionRequest
import com.ripenapps.adoreandroid.models.request_models.LikeDislikeRequest
import com.ripenapps.adoreandroid.models.request_models.LoginRequest
import com.ripenapps.adoreandroid.models.request_models.PlanCancelRequest
import com.ripenapps.adoreandroid.models.request_models.PlanPurchaseRequest
import com.ripenapps.adoreandroid.models.request_models.PlanRenewModel
import com.ripenapps.adoreandroid.models.request_models.ResetPasswordRequest
import com.ripenapps.adoreandroid.models.request_models.SendMessageRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyOtpRequest
import com.ripenapps.adoreandroid.models.request_models.SendOtpRequest
import com.ripenapps.adoreandroid.models.request_models.SocialLoginRequest
import com.ripenapps.adoreandroid.models.request_models.UpdateLocationRequest
import com.ripenapps.adoreandroid.models.request_models.VerifyMobileLoginRequest
import com.ripenapps.adoreandroid.networkcall.RetrofitBuilder
import okhttp3.MultipartBody

class ApiRepository {
    private val service = RetrofitBuilder.apiService
    suspend fun questionListApi() = service.questionList()
    suspend fun updateUserDataApi(userId: String) = service.updateUserData(userId)
    suspend fun interestListApi() = service.interestList()
    suspend fun hobbyListApi() = service.hobbyList()
    suspend fun whatElseYouLikeApi() = service.whatElseYouLike()
    suspend fun registerUserApi(requestBody: MultipartBody) = service.registerUser(requestBody)
    suspend fun sendOtpSignupApi(request: SendOtpRequest) = service.sendOtpSignup(request)
    suspend fun verifyOtpApi(request: VerifyOtpRequest) = service.verifyOtp(request)
    suspend fun reSendOtpApi(request: SendOtpRequest) = service.resendOtpSignUp(request)
    suspend fun loginResponseApi(request: LoginRequest) = service.userLogin(request)
    suspend fun deleteAccountVerifyApi(request: LoginRequest) = service.deleteAccountVerify(request)
    suspend fun sendOtpLoginApi(request: SendOtpRequest) = service.sendOtpLogin(request)
    suspend fun resendOtpLoginApi(request: SendOtpRequest) = service.resendOtpLogin(request)
    suspend fun verifyOtpLoginApi(request: VerifyOtpRequest) = service.verifyOtpLogin(request)
    suspend fun resetPasswordResponseApi(request: ResetPasswordRequest) =
        service.resetPasswordLogin(request)

    suspend fun verifyMobileLoginApi(request: VerifyMobileLoginRequest) =
        service.verifyMobileLogin(request)

    suspend fun storyListingApi(token: String) = service.storyListing(token)

    suspend fun myStoryListApi(token: String) = service.myStoryList(token)

    suspend fun otherStoryList(token: String, userId: String) =
        service.otherStoryList(token, userId)

    suspend fun storyViewersListApi(token: String, userId: String) =
        service.storyViewersList(token, userId)

    suspend fun deleteStoryApi(token: String, userId: String) = service.deleteStory(token, userId)

    suspend fun viewStoryApi(token: String, storyId: String) = service.viewStory(token, storyId)

    suspend fun uploadStoryApi(token: String, requestBody: MultipartBody) =
        service.uploadStory(token, requestBody)

    suspend fun cardListApi(
        token: String,
        page: Int,
        limit: Int,
        city: String,
        minAge: Int,
        maxAge: Int,
        minDistance: Int,
        maxDistance: Int,
        relationshipGoal: String,
        complexion: String,
        interestedIn: String,
        sexualOrientation: String,
        sortBy: String,
        whatElseYouLike: MutableList<String>
    ) =
        service.cardList(
            token,
            page,
            limit,
            if (city.isNullOrEmpty()) null else city,
            if (minAge == 0) null else minAge,
            if (maxAge == 0) null else maxAge,
            if (minDistance == -1) null else minDistance,
            if (maxDistance == 0) null else maxDistance,
            if (relationshipGoal.isNullOrEmpty()) null else relationshipGoal,
            if (complexion.isNullOrEmpty()) null else complexion,
            if (interestedIn.isNullOrEmpty()) null else interestedIn,
            if (sexualOrientation.isNullOrEmpty()) null else sexualOrientation,
            if (sortBy.isNullOrEmpty()) null else sortBy,
            if (whatElseYouLike.isNullOrEmpty()) null else whatElseYouLike
        )

    suspend fun likeDislikeUserApi(token: String, request: LikeDislikeRequest, userId: String) =
        service.likeDislikeUser(token, request, userId)

    suspend fun cardDetailsApi(token: String, userId: String) = service.cardDetails(token, userId)

    suspend fun gendersListApi() = service.gendersList()

    suspend fun userProfileDetailsApi(token: String) = service.getUserProfileDetails(token)

    suspend fun updateUserProfileApi(token: String, requestBody: MultipartBody) =
        service.updateUserProfile(token, requestBody)

    suspend fun updateGalleryMediaApi(token: String, requestBody: MultipartBody) =
        service.updateGalleryMedia(token, requestBody)

    suspend fun connectionListApi(token: String, status: String) =
        service.connectionListing(token, status)

    suspend fun changePasswordApi(token: String, request: ChangePasswordRequest) =
        service.changePassword(token, request)

    suspend fun termsAndPolicyApi(token: String, type: String) = service.termsAndPolicy(token, type)

    suspend fun deleteAccountApi(token: String/*, request:DeleteRequestModel*/) =
        service.deleteAccount(token/*, request*/)

    suspend fun sendChatMediaApi(token: String, requestBody: MultipartBody) =
        service.sendChatMedia(token, requestBody)

    suspend fun getFaqApi(token: String) = service.getFaq(token)

    suspend fun getHelpChatListApi(token: String) = service.getHelpChatList(token)

    suspend fun sendHelpMessageApi(token: String, request: SendMessageRequest) =
        service.sendHelpMessage(token, request)

    suspend fun userSearchListApi(
        token: String,
        search: String? = null,
        city: String,
        minAge: Int,
        maxAge: Int,
        minDistance: Int,
        maxDistance: Int,
        sortBy: String,
        interestedIn: String,
    ) =
        service.userSearchList(
            token,
            if (search.isNullOrEmpty()) null else search,
            if (city.isNullOrEmpty()) null else city,
            if (minAge == 0) null else minAge,
            if (maxAge == 0) null else maxAge,
            if (minDistance == -1) null else minDistance,
            if (maxDistance == 0) null else maxDistance,
            if (sortBy.isNullOrEmpty()) null else sortBy,
            if (interestedIn.isNullOrEmpty()) null else interestedIn
        )

    suspend fun updateLocationApi(token: String, request: UpdateLocationRequest) =
        service.updateLocation(token, request)

    suspend fun boostProfileApi(token: String) = service.boostProfile(token)

    suspend fun notificationListApi(
        token: String,
        type: String,
        page: String,
        limit: String,
        senderId: String
    ) =
        service.notificationList(
            token,
            if (type.isNullOrEmpty()) null else type,
            if (page.isNullOrEmpty()) null else page,
            if (limit.isNullOrEmpty()) null else limit,
            if (senderId.isNullOrEmpty()) null else senderId
        )

    suspend fun socialLoginApi(request: SocialLoginRequest) = service.socialLogin(request)

    suspend fun getPlanApi(token: String) = service.planList(token)

    suspend fun deleteNotificationApi(
        token: String,
        notificationId: String,
        status: String,
        senderId: String,
        roomId:String,
        chatId:String
    ) =
        service.deleteNotification(
            token,
            if (notificationId.isNullOrEmpty()) null else notificationId,
            if (status.isNullOrEmpty()) null else status,
            if (senderId.isNullOrEmpty()) null else senderId,
            if (roomId.isNullOrEmpty()) null else roomId,
            if (chatId.isNullOrEmpty()) null else chatId
        )

    suspend fun logoutApi(token: String) = service.logout(token)

    suspend fun planPurchaseApi(token: String, request:PlanPurchaseRequest) =
        service.planPurchase(
            token,
            request = request
        )
    suspend fun planRenewApi(token: String, request: PlanRenewModel) =
        service.planRenew(
            token,
            request
        )
    suspend fun planCancelApi(token: String, request:PlanCancelRequest) =
        service.planCancel(
            token,
            request
        )
    suspend fun checkVersionApi(deviceType: String) = service.checkVersion(deviceType)
}