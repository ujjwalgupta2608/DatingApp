package com.ripenapps.adoreandroid.models.response_models.connectionlist

data class LoginLocation(
    val coordinates: MutableList<Double>?= mutableListOf(),
    val type: String?=""
)