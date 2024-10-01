package com.ripenapps.adoreandroid.models.response_models.likeUnlikeResponse

data class Data(
    val distance:String?="",
    val receiver: Receiver?,
    val roomId: String?="",
    val sender: Sender?,
    val statusReceiver: String?="",
    val statusSender: String?=""
)