package com.ripenapps.adoreandroid.models.response_models.chatMediaList

import android.os.Parcelable
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Result(
    val __v: Int?=0,
    val _id: String?="",
    val createdAt: String?="",
    val isRead: Boolean?=false,
    val media: MutableList<Media>?= mutableListOf(),
    val messageType: String?="",
    val receiverId: String?="",
    val roomId: String?="",
    val updatedAt: String?=""
):Parcelable

@Parcelize
data class MediaList(
    val mediaList:MutableList<Result>?= mutableListOf()
):Parcelable