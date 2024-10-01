package com.ripenapps.adoreandroid.models.response_models.myStoryList

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryData(
    val story: MutableList<Story>,
    val roomId:String?=""
):Parcelable