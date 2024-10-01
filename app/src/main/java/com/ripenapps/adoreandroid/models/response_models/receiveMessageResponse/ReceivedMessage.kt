package com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse

data class ReceivedMessage(
    var online: String? = "",
    var lastSeen: String? = "",
    var blockUser: MutableList<String> = mutableListOf(),
    var like_status: String? = "",
    var status: Int? = 0,
    var message: String? = "",
    var result: MutableList<ReceivedMessageResponse> = mutableListOf()
)