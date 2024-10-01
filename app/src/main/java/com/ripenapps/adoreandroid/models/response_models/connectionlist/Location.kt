package com.ripenapps.adoreandroid.models.response_models.connectionlist

data class Location(
    val coordinates: MutableList<Double>? = mutableListOf(),
    val type: String?=""
)