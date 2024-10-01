package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.databinding.QuestionLookingForLayoutBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.models.response_models.getquestions.LookingForData
import com.ripenapps.adoreandroid.views.adapters.AdapterLookingFor

class LookingForVH(private val binding: QuestionLookingForLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var list: MutableList<LookingForData> = arrayListOf()

    fun bind(position: Int, list: MutableList<GenderData>, lookingForAdapterClick:(Int)->Unit, selectedLookingForPosition:Int) {
        binding.selectInterestRecyclerView.adapter = AdapterLookingFor(list, lookingForAdapterClick, selectedLookingForPosition)
    }
}