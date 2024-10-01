package com.ripenapps.adoreandroid.models.response_models.connectionlist

data class UserInfo(
    val __v: Int?=0,
    val _id: String?="",
    val address: String?="",
    val city: String?="",
    val age: Int?=0,
    val bioDescription: String?="",
    val countryCode: String?="",
    val createdAt: String?="",
    val deviceToken: String?="",
    val deviceType: String?="",
    val email: String?="",
    val gender: String?="",
    val lastEmailSentAt: String?="",
    val location: Location?,
    val mobile: String?="",
    val name: String?="",
    val notificationStatus: Boolean?=false,
    val orientationStatus: Boolean?=false,
    val password: String?="",
    val profile: String?="",
    val status: Boolean?=false,
    val userName: String?="",
    val is_online:String?=""
)