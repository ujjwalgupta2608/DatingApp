package com.ripenapps.adoreandroid.models.response_models.carddetails

import android.os.Parcelable
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val _id: String?="",
    val address: String?="",
    val city:String?="",
    val age: Int?=0,
    val bioDescription: String?="",
    val countryCode: String?="",
    val createdAt: String?="",
    val deviceToken: String?="",
    val deviceType: String?="",
    val email: String?="",
    val gender: String?="",
    val height: String?="",
    val language: String?="",
    val genderStatus: Boolean?=false,
    val interestedIn: String?="",
    val lifestyle: Lifestyle?=Lifestyle(),
    val media: MutableList<Media>?= mutableListOf(),
    val mobile: String?="",
    val name: String?="",
    val orientationStatus: Boolean?=false,
    val profile: String?="",
    val profileUrl:String?="",
    val relationshipGoal: String?="",
    val sexualOrientation: String?="",
    val status: Boolean?=false,
    val topInterests: MutableList<InterestData>?= mutableListOf(),
    val updatedAt: String?="",
    val userName: String?="",
    val whatElseYouLike: MutableList<WhatElseYouLike?>?= mutableListOf(),
    val whatMakesYou: WhatMakesYou=WhatMakesYou()
):Parcelable