package com.ripenapps.adoreandroid.models.request_models

data class SendOtpRequest(
    var email:String="",
    var mobile:String="",
    var countryCode:String="",
    var isDelete:String?=""
)