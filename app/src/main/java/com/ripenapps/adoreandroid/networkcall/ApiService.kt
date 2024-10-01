package com.ripenapps.adoreandroid.networkcall

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
import com.ripenapps.adoreandroid.models.response_models.CommonResponse
import com.ripenapps.adoreandroid.models.response_models.registerdata.RegisterUserResponse
import com.ripenapps.adoreandroid.models.response_models.LoginResponse
import com.ripenapps.adoreandroid.models.response_models.ResetPasswordResponse
import com.ripenapps.adoreandroid.models.response_models.SendMediaResponse
import com.ripenapps.adoreandroid.models.response_models.VerifyOtpResponse
import com.ripenapps.adoreandroid.models.response_models.getquestions.QuestionsDataResponse
import com.ripenapps.adoreandroid.models.response_models.SendOtpResponse
import com.ripenapps.adoreandroid.models.response_models.TermsAndPolicyResponse
import com.ripenapps.adoreandroid.models.response_models.VerifyMobileLoginResponse
import com.ripenapps.adoreandroid.models.response_models.WhatElseYouLikeResponse
import com.ripenapps.adoreandroid.models.response_models.boostData.BoostDataResponse
import com.ripenapps.adoreandroid.models.response_models.carddetails.CardDetailsResponse
import com.ripenapps.adoreandroid.models.response_models.cardlist.CardListResponse
import com.ripenapps.adoreandroid.models.response_models.checkversionresponse.CheckVersionResponse
import com.ripenapps.adoreandroid.models.response_models.connectionlist.ConnectionListResponse
import com.ripenapps.adoreandroid.models.response_models.faqResponse.GetFaqResponse
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestListResponse
import com.ripenapps.adoreandroid.models.response_models.helpList.HelpListResponse
import com.ripenapps.adoreandroid.models.response_models.likeUnlikeResponse.LikeUnlikeResponse
import com.ripenapps.adoreandroid.models.response_models.myStoryList.MyStoryResponse
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationResponse
import com.ripenapps.adoreandroid.models.response_models.planList.PlanListResponse
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListingResponse
import com.ripenapps.adoreandroid.models.response_models.storyViewers.StoryViewersResponse
import com.ripenapps.adoreandroid.models.response_models.updategallery.UpdateGalleryResponse
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.UserSearchListResponse
import com.ripenapps.adoreandroid.models.response_models.userprofiledetails.UserProfileDetailsResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/user/sample/get/alldata")    //used during registration
    suspend fun questionList(): QuestionsDataResponse

    @GET("/user/sample/get/alldata")   //used during update profile
    suspend fun updateUserData(@Query("userId") userId: String): QuestionsDataResponse

    @GET("/user/sample/interest/list")
    suspend fun interestList(): InterestListResponse

    @GET("/user/sample/hobby/list")
    suspend fun hobbyList(): InterestListResponse

    @GET("/user/sample/hobby/list")
    suspend fun whatElseYouLike(): WhatElseYouLikeResponse

    @POST("/user/auth/register")
    suspend fun registerUser(@Body requestBody: MultipartBody): RegisterUserResponse

    @POST("/user/verify/sendOtp")
    suspend fun sendOtpSignup(@Body request: SendOtpRequest): SendOtpResponse

    @POST("/user/verify/verifyOtp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("/user/verify/reSendOtp")
    suspend fun resendOtpSignUp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("/user/auth/login")
    suspend fun userLogin(@Body request: LoginRequest): LoginResponse

    @POST("/user/auth/pass_varify")
    suspend fun deleteAccountVerify(@Body request: LoginRequest):LoginResponse

    @POST("/user/auth/sendOtp")
    suspend fun sendOtpLogin(@Body request: SendOtpRequest): SendOtpResponse

    @POST("/user/auth/reSendOtp")
    suspend fun resendOtpLogin(@Body request: SendOtpRequest): SendOtpResponse

    @POST("/user/auth/verify")
    suspend fun verifyOtpLogin(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("/user/auth/reset_password")
    suspend fun resetPasswordLogin(@Body request: ResetPasswordRequest): ResetPasswordResponse

    @POST("/user/auth/verify_mobile_login")
    suspend fun verifyMobileLogin(@Body request: VerifyMobileLoginRequest): VerifyMobileLoginResponse

    @GET("/user/stories/get_story")
    suspend fun storyListing(@Header("Authorization") token: String): StoryListingResponse

    @GET("/user/stories/my_story")
    suspend fun myStoryList(@Header("Authorization") token: String): MyStoryResponse

    @GET("/user/stories/my_story")
    suspend fun otherStoryList(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): MyStoryResponse

    @GET("/user/stories/story_viewers/{userId}")
    suspend fun storyViewersList(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): StoryViewersResponse

    @DELETE("user/stories/delete_story/{userId}")
    suspend fun deleteStory(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): CommonResponse

    @GET("user/stories/view_story/{storyId}")
    suspend fun viewStory(
        @Header("Authorization") token: String,
        @Path("storyId") storyId: String
    ): CommonResponse

    @POST("user/stories/upload_story")
    suspend fun uploadStory(
        @Header("Authorization") token: String,
        @Body requestBody: MultipartBody
    ): CommonResponse

    @GET("user/card/list")
    suspend fun cardList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("city") city: String? = null,
        @Query("minAge") minAge: Int? = null,
        @Query("maxAge") maxAge: Int? = null,
        @Query("minDistance") minDistance: Int? = null,
        @Query("maxDistance") maxDistance: Int? = null,
        @Query("relationshipGoal") relationshipGoal: String? = null,
        @Query("complexion") complexion: String? = null,
        @Query("interestedIn") interestedIn: String? = null,
        @Query("sexualOrientation") sexualOrientation: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("whatElseYouLike") whatElseYouLike: MutableList<String>? = null,
    ): CardListResponse

    @POST("user/card/likeDislike/{userId}")
    suspend fun likeDislikeUser(
        @Header("Authorization") token: String,
        @Body request: LikeDislikeRequest,
        @Path("userId") userId: String
    ): LikeUnlikeResponse

    @GET("user/card/detailes/{userId}")
    suspend fun cardDetails(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): CardDetailsResponse

    @GET("user/sample/gender/list")
    suspend fun gendersList(): InterestListResponse

    @GET("user/user_profile/detailes")
    suspend fun getUserProfileDetails(@Header("Authorization") token: String): UserProfileDetailsResponse

    @PUT("user/user_profile/update")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body requestBody: MultipartBody
    ): CommonResponse

    @POST("user/user_profile/upload_gallery")
    suspend fun updateGalleryMedia(
        @Header("Authorization") token: String,
        @Body requestBody: MultipartBody
    ): UpdateGalleryResponse

    @GET("user/connection/my_connection")
    suspend fun connectionListing(
        @Header("Authorization") token: String,
        @Query("connection") status: String
    ): ConnectionListResponse

    @PUT("user/setting/change_password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): CommonResponse

    @GET("user/setting/get_content")
    suspend fun termsAndPolicy(
        @Header("Authorization") token: String,
        @Query("type") type: String
    ): TermsAndPolicyResponse

    @DELETE("user/setting/account_remove")
    suspend fun deleteAccount(@Header("Authorization") token: String): CommonResponse

    @POST("user/user_profile/upload_chat_media")
    suspend fun sendChatMedia(
        @Header("Authorization") token: String,
        @Body requestBody: MultipartBody
    ): SendMediaResponse

    @GET("user/setting/get_faq")
    suspend fun getFaq(@Header("Authorization") token: String): GetFaqResponse

    @GET("user/support/chat_list")
    suspend fun getHelpChatList(@Header("Authorization") token: String): HelpListResponse

    @POST("user/support/send_message")
    suspend fun sendHelpMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): CommonResponse

    @GET("user/user_profile/user_search_list")
    suspend fun userSearchList(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("city") city: String? = null,
        @Query("minAge") minAge: Int? = null,
        @Query("maxAge") maxAge: Int? = null,
        @Query("minDistance") minDistance: Int? = null,
        @Query("maxDistance") maxDistance: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("interestedIn") interestedIn: String? = null,
    ): UserSearchListResponse

    @POST("user/user_profile/update_location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body request: UpdateLocationRequest
    ): CommonResponse

    @POST("user/boost/boost_profile")
    suspend fun boostProfile(@Header("Authorization") token: String): BoostDataResponse

    @GET("user/setting/notification_list")
    suspend fun notificationList(
        @Header("Authorization") token: String,
        @Query("notificationType") type: String? = null,
        @Query("page") page: String? = null,
        @Query("limit") limit: String? = null,
        @Query("senderId") senderId: String? = null
    ): NotificationResponse

    @POST("user/auth/social_login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): CardDetailsResponse

    @GET("user/setting/get_plan")
    suspend fun planList(@Header("Authorization") token: String): PlanListResponse

    @GET("user/setting/notification_remove")
    suspend fun deleteNotification(
        @Header("Authorization") token: String,
        @Query("notificationId") notificationId: String? = null,
        @Query("status") status: String? = null,
        @Query("senderId") senderId: String? = null,
        @Query("roomId") roomId: String? = null,
        @Query("chatId") chatId: String? = null
    ): CommonResponse

    @POST("user/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): CommonResponse

    @POST("user/setting/purchase_plan")
    suspend fun planPurchase(
        @Header("Authorization") token: String,
        @Body request:PlanPurchaseRequest
    ): CommonResponse

    @POST("user/setting/renew_plan")
    suspend fun planRenew(
        @Header("Authorization") token: String,
        @Body request: PlanRenewModel
    ): CommonResponse

    @POST("user/setting/purchase_plan_cancel")
    suspend fun planCancel(
        @Header("Authorization") token: String,
        @Body request:PlanCancelRequest
    ): CommonResponse

    @GET("user/user_profile/version")
    suspend fun checkVersion(@Query("deviceType") deviceType: String): CheckVersionResponse
    /*@GET("applications/{packageName}/purchases/subscriptions/{subscriptionId}/tokens/{token}")
    suspend fun getSubscription(
        @Path("packageName") packageName: String,
        @Path("subscriptionId") subscriptionId: String,
        @Query("token") token: String,
        @Header("Authorization") accessToken: String
    ): Response<CommonResponse>*/
}
/*
interface SubscriptionApiService {
    @GET("applications/{packageName}/purchases/subscriptions/{subscriptionId}/tokens/{token}")
    suspend fun getSubscription(
        @Path("packageName") packageName: String,
        @Path("subscriptionId") subscriptionId: String,
        @Path("token") token: String,
        @Header("Authorization") accessToken: String
    ): Response<CommonResponse>
}*/
