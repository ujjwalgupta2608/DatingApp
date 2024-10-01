package com.ripenapps.adoreandroid.models.request_models

data class VerifyMobileLoginRequest(
    var mobile: String? = "",
    var resetPasswordOTP: String? = "",
    var countryCode: String? = "",
    var deviceToken: String? = "",
    var deviceType: String? = "",

    )