package com.ripenapps.adoreandroid.models.response_models.cardlist

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CardListUser(
    val _id: String?,
    val age: String?,
    val createdAt: String?,
    val distance: Double?,
    val city:String?="",
    var media: MutableList<String> = mutableListOf(),
    val name: String?,
    val profile: String?,
    val profileUrl: String?,
    val status: Boolean?,
    val title:String?,
    val description:String?,
    val bannerImage:String?,
    val bannerLink:String?,
    val type:String?,
    ):Parcelable