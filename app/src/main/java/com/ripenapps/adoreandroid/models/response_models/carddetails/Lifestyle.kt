package com.ripenapps.adoreandroid.models.response_models.carddetails

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Lifestyle(
    val drink: String?="",
    val profession: String?="",
    val smoke: String?=""
):Parcelable