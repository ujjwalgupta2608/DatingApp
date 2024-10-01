package com.ripenapps.adoreandroid.models.response_models

data class VerifyMobileLoginResponse(
    var status: String? = "",
    var message: String? = "",
    val data: LoginData

)
