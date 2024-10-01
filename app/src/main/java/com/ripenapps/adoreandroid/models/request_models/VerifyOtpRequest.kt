package com.ripenapps.adoreandroid.models.request_models

data class VerifyOtpRequest(
    var countryCode:String? = "",
    var email:String? = "",
    var otpEmail:String? = "",
    var mobile:String? = "",
    var mobileOtp:String? = "",
    var resetPasswordOTP:String? = ""
)
