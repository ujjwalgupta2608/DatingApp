package com.ripenapps.adoreandroid.models.response_models.getquestions

data class MakesYouData(
    val __v: Int,
    val _id: String,
    val communication: List<String>,
    val education: List<String>,
    val status: Boolean,
    val zodiac: List<String>
)