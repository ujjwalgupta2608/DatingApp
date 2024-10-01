package com.ripenapps.adoreandroid.models.response_models.connectionlist

data class Listing(
    val _id: String?="",
    val createdAt: String?="",
    val distance: Double?=0.0,
    val userStatus:String?="",
    val loginLocation: LoginLocation=LoginLocation(),
    val receiver: String?="",
    val sender: String?="",
    val status: String?="",
    val userInfo: UserInfo?
)