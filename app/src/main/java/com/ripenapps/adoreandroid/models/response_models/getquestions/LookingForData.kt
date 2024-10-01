package com.ripenapps.adoreandroid.models.response_models.getquestions

data class LookingForData(
    val __v: Int=0,
    val _id: String?="",
    val emoji: String?="",
    val name: String?="",
    val status: Boolean=false,
    var isSelected: Boolean = false
)