package com.ripenapps.adoreandroid.models.response_models.userprofiledetails

import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData

data class User(
    val __v: Int?,
    val _id: String?,
    val address: String?,
    val age: Int?,
    val bioDescription: String?,
    val complexion: String?,
    val countryCode: String?,
    val createdAt: String?,
    val deviceToken: String?,
    val deviceType: String?,
    val email: String?,
    val gender: String?,
    val genderStatus: Boolean?,
    val height: String?,
    val id: String?,
    val interestedIn: String?,
    val language: MutableList<String>?,
    val lastEmailSentAt: String?,
    val lifestyle: Lifestyle?,
    val location: Location?,
    val media: List<Any?>?,
    val mediaUrl: String?,
    val mobile: String?,
    val name: String?,
    val notificationStatus: Boolean?,
    val orientationStatus: Boolean?,
    val password: String?,
    val profile: String?,
    val profileUrl: String?,
    val relationshipGoal: String?,
    val sexualOrientation: String?,
    val status: Boolean?,
    val topInterests: MutableList<InterestData?>?,
    val updatedAt: String?,
    val userName: String?,
    val whatElseYouLike: MutableList<InterestData>,
    val whatMakesYou: WhatMakesYou?
)