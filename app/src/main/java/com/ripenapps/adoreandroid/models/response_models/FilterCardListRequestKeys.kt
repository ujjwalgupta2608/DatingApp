package com.ripenapps.adoreandroid.models.response_models

data class FilterCardListRequestKeys(
    var city:String="",
    var search: String = "",
    var minAge: Int = 18,
    var maxAge: Int = 30,
    var minDistance: Int = 0, // 0 can't be default as 0km can be selected in seekbar that can not be taken as empty null check for passing it as query in api.
    var maxDistance: Int = 20,
    var relationshipGoal: String = "",
    var sexualOrientation: String = "",
    var interestedIn: String = "",
    var complexion: String = "",
    var sortBy: String = "",
    var whatElseYouLike: MutableList<String> = mutableListOf(),
    var lat:Double=0.0,
    var long:Double=0.0

)
