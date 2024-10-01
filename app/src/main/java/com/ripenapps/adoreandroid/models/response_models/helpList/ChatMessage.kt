package com.ripenapps.adoreandroid.models.response_models.helpList

data class ChatMessage(
    val __v: Int?=0,
    val _id: String?="",
    var dateToShow:String="",
    val createdAt: String?="",
    val message: String?="",
    val receiver: String?="",
    val sender: String?=""
)