package com.ripenapps.adoreandroid.models.response_models.carddetails

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhatElseYouLike(
    val _id: String?="",
    val icon: String?="",
    val name: String?=""
):Parcelable