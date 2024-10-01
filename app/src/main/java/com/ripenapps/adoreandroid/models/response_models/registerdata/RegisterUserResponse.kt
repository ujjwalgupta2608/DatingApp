package com.ripenapps.adoreandroid.models.response_models.registerdata

data class RegisterUserResponse (
    var message:String="",
    var status:String="",
    var data:RegisterUserData?
)