package com.ripenapps.adoreandroid.models.response_models.cardlist

data class BoostData(
    var startTime:String?="",
    var endTime:String?="",
    var boostCount:Int?=-1,
    var boostStatus:Boolean?=false,
    var hasActivePlan:Boolean?=false,
    var subscriptionPurchaseDate:String?="",
    var subscriptionExpireDate:String?=""
    )
