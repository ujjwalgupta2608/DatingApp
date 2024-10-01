package com.ripenapps.adoreandroid.models.response_models.notificationlist

data class Data(
    val list: MutableList<NotificationData>? = mutableListOf(),
    val total: Int?=0
)