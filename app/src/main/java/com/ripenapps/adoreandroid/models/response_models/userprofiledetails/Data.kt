package com.ripenapps.adoreandroid.models.response_models.userprofiledetails

data class Data(
    val user: User?,
    val userMedia: MutableList<com.ripenapps.adoreandroid.models.response_models.carddetails.Media>?
)