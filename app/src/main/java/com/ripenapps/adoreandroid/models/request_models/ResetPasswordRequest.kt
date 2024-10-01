package com.ripenapps.adoreandroid.models.request_models

data class ResetPasswordRequest(
    var newPassword: String? = "",
    var confirmPassword: String? = "",
    var resetPasswordOTP: String? = "",
    var email: String? = "",
    var mobile:String="",
    var countryCode:String=""
    )