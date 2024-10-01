package com.ripenapps.adoreandroid.models.response_models.carddetails

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Media(
    val _id: String?="",
    val image: String?="",
    val type: String?="",
    val url: String?="",
    val video: String?="",
    val thumbnail: String?="",
    val videoThumbnail: String?="",
    var isSelected: Boolean =false
):Parcelable
@Parcelize
data class MediaList(
    val mediaList:MutableList<Media>?= mutableListOf()
):Parcelable