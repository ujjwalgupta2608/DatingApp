package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.QuestionHabitsLayoutBinding
import com.ripenapps.adoreandroid.models.StepSevenEightModel
import com.ripenapps.adoreandroid.views.adapters.AdaptorOtherDetailsList

class SelectOtherDetailsVH(val binding: QuestionHabitsLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(detailsList: MutableList<StepSevenEightModel>,position: Int) {
        binding.title.text = itemView.resources.getString(R.string.what_else_makes_you_you)
        binding.questionsRecycler.adapter = AdaptorOtherDetailsList(detailsList)
    }
}