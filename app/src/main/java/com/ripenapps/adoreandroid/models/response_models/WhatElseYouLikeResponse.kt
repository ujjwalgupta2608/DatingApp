package com.ripenapps.adoreandroid.models.response_models

import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData

data class WhatElseYouLikeResponse(
    val data: MutableList<GenderData> = mutableListOf(),
    val message: String?="",
    val status: Int?=0
)
