package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.databinding.QuestionSelectInterestsLayoutBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData
import com.ripenapps.adoreandroid.views.adapters.AdapterSelectInterests
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class SelectInterestsVH(private val binding: QuestionSelectInterestsLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var layoutManager = FlexboxLayoutManager(binding.root.context)
    fun bind(position: Int,list: MutableList<InterestData>, selectInterestsAdapterClick:(List<InterestData>)->Unit, selectInterestsItemClick: (Int) -> Unit) {
        layoutManager.flexDirection = FlexDirection.ROW;
        layoutManager.justifyContent = JustifyContent.CENTER;
        layoutManager.alignItems = AlignItems.CENTER;
        binding.selectInterestRecyclerView.layoutManager = layoutManager
        (binding.selectInterestRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.selectInterestRecyclerView.adapter = AdapterSelectInterests(list, selectInterestsAdapterClick)
    }

}