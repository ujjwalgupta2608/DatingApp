package com.ripenapps.adoreandroid.models.response_models.planList

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Plan(
    val _id: String?="",
    val benifits: MutableList<String>? = mutableListOf(),
    val createdAt: String?="",
    val description: String?="",
    val discountPrice: Int?=0,
    val duration: Int?=0,
    val inAppPurchasePlanId: String?="",
    val price: Int?=0,
    val status: Boolean?=false,
    val title: String?="",
    val type: String?="",
    val updatedAt: String?=""
):Parcelable