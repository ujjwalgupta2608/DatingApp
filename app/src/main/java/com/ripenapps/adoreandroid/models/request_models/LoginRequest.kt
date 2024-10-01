package com.ripenapps.adoreandroid.models.request_models

data class LoginRequest(
    var deviceToken: String="",
    var deviceType: String="",
    var email: String="",
    var password: String="",
    var userName: String=""
)