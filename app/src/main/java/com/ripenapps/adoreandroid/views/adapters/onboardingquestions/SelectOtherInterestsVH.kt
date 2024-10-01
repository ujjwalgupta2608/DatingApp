package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.databinding.QuestionSelectInterestsLayoutBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData
import com.ripenapps.adoreandroid.views.adapters.AdaptorSelectOtherInterests
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.ripenapps.adoreandroid.R

class SelectOtherInterestsVH(private val binding:QuestionSelectInterestsLayoutBinding): RecyclerView.ViewHolder(binding.root) {
    var layoutManager = FlexboxLayoutManager(binding.root.context)
    lateinit var adapterSelectOtherInterests : AdaptorSelectOtherInterests
    fun bind(position:Int, list: MutableList<InterestData>, otherInterestsAdapterClick:(List<InterestData>)->Unit){
        binding.title.text = itemView.resources.getString(R.string.what_else_you_like_more)
        adapterSelectOtherInterests = AdaptorSelectOtherInterests(list, otherInterestsAdapterClick)
        layoutManager.flexDirection = FlexDirection.ROW;
        layoutManager.justifyContent = JustifyContent.CENTER;
        layoutManager.alignItems = AlignItems.CENTER;
        binding.selectInterestRecyclerView.layoutManager = layoutManager
        binding.selectInterestRecyclerView.adapter = adapterSelectOtherInterests
        (binding.selectInterestRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        adapterSelectOtherInterests.notifyDataSetChanged()
        binding.selectInterestRecyclerView.adapter = AdaptorSelectOtherInterests(list, otherInterestsAdapterClick)
    }


}