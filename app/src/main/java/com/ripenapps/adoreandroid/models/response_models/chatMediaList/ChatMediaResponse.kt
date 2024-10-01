package com.ripenapps.adoreandroid.models.response_models.chatMediaList

data class ChatMediaResponse(
    val message: String?="",
    val result: MutableList<Result>?= mutableListOf(),
    val status: Int?=0
)