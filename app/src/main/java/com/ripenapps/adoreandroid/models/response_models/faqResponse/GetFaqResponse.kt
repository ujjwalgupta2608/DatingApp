package com.ripenapps.adoreandroid.models.response_models.faqResponse

data class GetFaqResponse(
    val data: Data?=Data(),
    val message: String?="",
    val status: Int?=0
)