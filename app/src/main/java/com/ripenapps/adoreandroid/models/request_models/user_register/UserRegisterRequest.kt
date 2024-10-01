package com.ripenapps.adoreandroid.models.request_models.user_register

import java.io.File

data class UserRegisterRequest(
    var name:String? = "",
    var email:String? = "",
    var mobile:String? = "",
    var gender:String? = "",
    var password:String? = "",
    var sexualOrientation:String? = "",
    var interestedIn:String? = "",
    var whatElseYouLike:MutableList<String>? = mutableListOf(),
    var topInterests:MutableList<String>? = mutableListOf(),
    var relationshipGoal:String? = "",
    var userName:String? = "",
    var bioDescription:String? = "",
    var address:String? = "",
    var age:String? = "",
    var deviceToken:String? = "",
    var deviceType:String? = "",
    var lifestyle:LifeStyleHabbitData? = LifeStyleHabbitData(),
    var profile:File?,
    var whatMakesYou:LifeStyleHabbitData? = LifeStyleHabbitData(),
    var photos:List<File>
)