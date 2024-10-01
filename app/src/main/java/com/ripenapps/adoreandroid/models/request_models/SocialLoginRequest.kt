package com.ripenapps.adoreandroid.models.request_models

data class SocialLoginRequest(
    var email:String?="",
    var name:String?="",
    var deviceToken:String?="",
    var deviceType:String?="",
    var socialType:String?="",
    var socialId:String?="",
    )
