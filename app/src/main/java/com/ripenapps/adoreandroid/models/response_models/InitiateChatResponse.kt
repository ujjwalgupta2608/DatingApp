package com.ripenapps.adoreandroid.models.response_models

data class InitiateChatResponse(
    var roomId:String?="",
    var receiverId:String?="",
    var messageType:String?=""
)
