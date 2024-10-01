package com.ripenapps.adoreandroid.models.response_models.getInterestList

data class InterestListResponse(
    val data: List<InterestData>,
    val message: String,
    val status: Int
)