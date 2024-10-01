package com.ripenapps.adoreandroid.models.response_models

data class SocketOnlineStatusResponse(
    var status:Int?=0,
    var message:String?="",
    var online:String?="",
    var lastSeen:String?="",
    )
