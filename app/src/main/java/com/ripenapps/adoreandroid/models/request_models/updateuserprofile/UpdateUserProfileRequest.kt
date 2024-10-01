package com.ripenapps.adoreandroid.models.request_models.updateuserprofile

data class UpdateUserProfileRequest(
    var address: String?="",
    var age: String?="",
    var bioDescription: String?="",
    var complexion: String?="",
    var deviceToken: String?="",
    var deviceType: String?="",
    var email: String?="",
    var gender: String?="",
    var height: String?="",
    var interestedIn: String?="",
    var language: String?="",
    var lifestyle: Lifestyle?= Lifestyle(),
    var mobile: String?="",
    var name: String?="",
    var relationshipGoal: String?="",
    var sexualOrientation: String?="",
    var userName: String?="",
    var whatMakesYou: WhatMakesYou?=WhatMakesYou(),
    var topInterests:ArrayList<String> = arrayListOf(),
    var whatElseYouLike:ArrayList<String> = arrayListOf()

)