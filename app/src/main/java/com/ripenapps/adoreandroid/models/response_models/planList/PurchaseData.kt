package com.ripenapps.adoreandroid.models.response_models.planList

data class PurchaseData(
    val _id: String?="",
    val createdAt: String?="",
    val expiryDate: String?="",
    val inAppPurchasePlanId: String?="",
    val isCancelled: Boolean?=false,
    val plan: String?="",
    val price: Int?=0,
    val purchaseDate: String?="",
    val updatedAt: String?="",
    val user: String?=""
)