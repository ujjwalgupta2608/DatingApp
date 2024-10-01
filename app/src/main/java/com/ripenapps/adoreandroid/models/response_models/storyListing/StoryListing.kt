package com.ripenapps.adoreandroid.models.response_models.storyListing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoryListing(
    val _id: String?="",
    val isView: Boolean? = false,
    val name: String?="",
    val isOnline:String?="",
    val profile: String?="",
    val storyId: List<String> = mutableListOf(),
    val user: String?="",
    val userName: String?=""
):Parcelable
