package com.ripenapps.adoreandroid.models.request_models

data class ChangePasswordRequest (
    var newPassword:String?="",
    var confirmPassword:String?="",
    var oldPassword:String?=""
)