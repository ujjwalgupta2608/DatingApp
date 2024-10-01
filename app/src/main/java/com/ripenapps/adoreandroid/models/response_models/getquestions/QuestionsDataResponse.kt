package com.ripenapps.adoreandroid.models.response_models.getquestions

data class QuestionsDataResponse(
    val data: Data=Data(),
    val message: String?="",
    val status: Int?=0
)