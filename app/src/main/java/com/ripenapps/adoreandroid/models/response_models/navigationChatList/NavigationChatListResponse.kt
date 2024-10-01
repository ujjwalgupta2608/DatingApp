package com.ripenapps.adoreandroid.models.response_models.navigationChatList

data class NavigationChatListResponse(
    val message: String?="",
    val result: MutableList<Result>? = mutableListOf(),
    val status: Int?=0
)