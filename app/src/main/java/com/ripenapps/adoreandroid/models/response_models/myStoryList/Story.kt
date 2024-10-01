package com.ripenapps.adoreandroid.models.response_models.myStoryList

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Story(
    val _id: String?=null,
    val media: String?=null,
    val type: String?=null,
    val viewerCount:Int?
):Parcelable