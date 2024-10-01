package com.ripenapps.adoreandroid.models.response_models.getInterestList

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InterestData(
    val _id: String="",
    val name: String="",
    var isSelected:Boolean = false,
    val icon:String="",
    val iconeUrl:String=""
):Parcelable