package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.databinding.QuestionHabitsLayoutBinding
import com.ripenapps.adoreandroid.models.StepSevenEightModel
import com.ripenapps.adoreandroid.views.adapters.AdapterHabitsList

class SelectHabitsVH(private val binding: QuestionHabitsLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(habitsList:MutableList<StepSevenEightModel>,position: Int) {
        binding.questionsRecycler.adapter = AdapterHabitsList(habitsList)
    }
}