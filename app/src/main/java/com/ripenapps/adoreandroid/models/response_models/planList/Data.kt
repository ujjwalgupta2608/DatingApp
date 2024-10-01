package com.ripenapps.adoreandroid.models.response_models.planList

data class Data(
    val plan: MutableList<Plan>? = mutableListOf(),
    val purchase:PurchaseData?=PurchaseData()
)