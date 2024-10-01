package com.ripenapps.adoreandroid.models.response_models.checkversionresponse

data class CheckVersionResponse(
    val data: Data=Data(),
    val message: String?="",
    val status: Int?=0
)