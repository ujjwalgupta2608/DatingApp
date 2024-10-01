package com.ripenapps.adoreandroid.models.response_models.helpList

data class HelpListResponse(
    val data: Data?=Data(),
    val message: String?="",
    val status: Int?=0
)