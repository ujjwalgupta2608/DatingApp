package com.ripenapps.adoreandroid.models.response_models.cardlist

data class CardListData(
    val total: Int?,
    val user: MutableList<CardListUser> = mutableListOf(),
    val boostData:BoostData? = BoostData(),
    val interestedIn:String?=""
)