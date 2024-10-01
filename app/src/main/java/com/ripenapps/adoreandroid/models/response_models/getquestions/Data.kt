package com.ripenapps.adoreandroid.models.response_models.getquestions

data class Data(
    val genderData: List<GenderData> = mutableListOf(),
    val lifestyleData: List<LifestyleData> = mutableListOf(),
    val lookingForData: MutableList<GenderData> = mutableListOf(),
    val makesYouData: List<LifestyleData> = mutableListOf(),
    val complexionData:MutableList<GenderData> = mutableListOf(),
    val languageData:MutableList<GenderData> = mutableListOf(),
    val orientationData:MutableList<GenderData> = mutableListOf()
)