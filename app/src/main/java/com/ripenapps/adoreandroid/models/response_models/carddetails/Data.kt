package com.ripenapps.adoreandroid.models.response_models.carddetails

data class Data(
    val token:String?="",
    val user: User?,
    val like_status: String?="",
    val blockedUsers:MutableList<String> = mutableListOf(),
    val roomId:String?=""
)
