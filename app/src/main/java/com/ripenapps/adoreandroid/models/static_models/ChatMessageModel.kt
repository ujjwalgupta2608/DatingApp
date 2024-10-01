package com.ripenapps.adoreandroid.models.static_models

data class ChatMessageModel(
    val message:String?="",
    val messageType:Int?=-1,
    val mediaUrl:String?="",
    val mediaList: MutableList<ChatMessageModel> = mutableListOf()
)
