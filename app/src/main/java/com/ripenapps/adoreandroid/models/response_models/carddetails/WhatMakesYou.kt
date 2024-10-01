package com.ripenapps.adoreandroid.models.response_models.carddetails

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhatMakesYou(
    val communication: String?="",
    val educationLevel: String?="",
    val zodiacSign: String?=""
):Parcelable