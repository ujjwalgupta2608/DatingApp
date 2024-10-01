package com.ripenapps.adoreandroid.models.request_models

data class PlanRenewModel(
    var planId:String? = "",
    var inAppPurchaseId:String?="",
    var expiryDate:String?=""
)
