package com.ripenapps.adoreandroid.models.response_models.video_call

data class Result(
    val callStatus: String,
    val callToken: String,
    val channelName: String,
    val chatMessageId: String,
    val duration: Int,
    val message: String,
    val messageType: String,
    val receiverId: String,
    val roomId: String,
    val senderId: SenderId,
    val userType: String,
    val notificationId:String
)