package com.ripenapps.adoreandroid.models.request_models

data class UpdateLocationRequest(
    val latitude:Double?=0.0,
    val longitude:Double?=0.0,
    val city:String?="",
    val address:String?="",
    val country:String?=""
    )