package com.ripenapps.adoreandroid.models.response_models.updategallery

import com.ripenapps.adoreandroid.models.response_models.carddetails.Media

data class UpdatedUser(
    val __v: Int?=0,
    val _id: String?="",
    val createdAt: String?="",
    val id: String?="",
    val media: MutableList<Media>?,
    val updatedAt: String?="",
    val user: String?=""
)