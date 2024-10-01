package com.ripenapps.adoreandroid.models.response_models.boostData

data class BoostDataResponse(
    val data: Data?=Data(),
    val message: String?="",
    val status: Int?=0
)