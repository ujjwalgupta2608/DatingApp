package com.ripenapps.adoreandroid.models.response_models.planList

data class PlanListResponse(
    val data: Data? = Data(),
    val message: String?,
    val status: Int?
)