package com.ripenapps.adoreandroid.models

import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel

data class OnboardingUiModel(
    var stepOne: MutableList<GenderData>? = null,
    var stepTwo: MutableList<SelectGenderModel>? = null,
    var stepSeven: MutableList<StepSevenEightModel>? = null,
    var stepEight:MutableList<StepSevenEightModel>? = null,
    var stepSix: MutableList<GenderData>? = null,
)

//data class StepOneModel(
//    var isSelected: Boolean = false,
//    var title: String? = null
//)

//data class StepTwoModel(
//    var title: String? = null,
//    var isSelected: Boolean? = null,
//    var isShowGenderToProfile: Boolean? = null
//)

data class StepSevenEightModel(
    var selectedOptionPosition: Int? = -1,
    var isSelected: Boolean? = false,
    var title: String? = null,
    var optionsList: MutableList<OptionsList>? = null
)

data class OptionsList(
    var title: String? = null,
    var isOptionSelected:Boolean? = false,
)