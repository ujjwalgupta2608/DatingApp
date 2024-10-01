package com.ripenapps.adoreandroid.models.request_models

data class PlanPurchaseRequest(
    var planId:String? = "",
    var inAppPurchaseId:String?="",
    var expiryDate:String?="",
    var purchaseId:String?="",
    var status:String?=""
    )
