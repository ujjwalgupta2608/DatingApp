package com.ripenapps.adoreandroid.models.response_models.userSearchListResponse

import com.ripenapps.adoreandroid.models.response_models.connectionlist.Location


data class User(
    var _id: String?="",
    var distance: Double?=0.0,
    val email: String?="",
    var name: String?="",
    val profile: String?="",
    var profileUrl: String?="",
    var location: Location? = Location(),
    var city:String = "",
    /*val receivedInteractions: MutableList<SentInteraction>? = mutableListOf(),
    val sentInteractions: MutableList<SentInteraction>? = mutableListOf(),*/
    val totalInteractions: Int?=0,
    var userName: String?=""
)