package com.ripenapps.adoreandroid.models.static_models

data class HabitsListModel(
    val question: String,
    var isQuestionSelected: Boolean,
    val options: ArrayList<SelectGenderModel>,
    var selectedOptionPosition: Int = -1
)