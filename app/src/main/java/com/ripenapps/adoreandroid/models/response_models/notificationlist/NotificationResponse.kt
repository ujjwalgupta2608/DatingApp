package com.ripenapps.adoreandroid.models.response_models.notificationlist

data class NotificationResponse(
    val data: Data?= Data(),
    val message: String?="",
    val status: Int?=0
)