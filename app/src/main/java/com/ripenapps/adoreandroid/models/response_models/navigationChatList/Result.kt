package com.ripenapps.adoreandroid.models.response_models.navigationChatList

data class Result(
    val roomId:String?="",
    val _id: String?="",
    val lastMessage: String?="",
    val name: String?="",
    val profile: String?="",
    val unreadCount: Int?=0,
    val updatedAt:String?="",
    val is_Online:String?="",
    val messageType:String?=""
)