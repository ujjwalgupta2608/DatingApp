package com.ripenapps.adoreandroid.models.response_models

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData
)

data class LoginUser(

    val _id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val userName: String

)

data class LoginData(
    val user: LoginUser,
    val token: String
)
