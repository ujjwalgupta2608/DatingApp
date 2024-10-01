package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.QuestionSexualOrientationLayoutBinding
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel
import com.ripenapps.adoreandroid.utils.enums.QuestionSexualOrientationEnum
import com.ripenapps.adoreandroid.views.adapters.AdapterSexualOrientation

class SexualOrientationVH(private val binding: QuestionSexualOrientationLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private var adapter: AdapterSexualOrientation? = null

    fun bind(
        sexualOrientationList: MutableList<SelectGenderModel>,
        isShowOrientationChecked: Boolean,
        position: Int,
        sexualOrientationAdapterClick:(Int)->Unit,
        sexualOrientationSelectedPostion:Int,
        selectSexualityItemClick: (Int) -> Unit,
    ) {
        if (adapter == null) {
            binding.sexualOrientationRecycler.adapter =
                AdapterSexualOrientation(sexualOrientationList, sexualOrientationAdapterClick, sexualOrientationSelectedPostion)
        }
        if (isShowOrientationChecked) {
            binding.sexualOrientationCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.checked_box))
        } else {
            binding.sexualOrientationCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.unchecked_box))
        }
        binding.apply {
            sexualOrientationCheck.setOnClickListener {
                selectSexualityItemClick.invoke(QuestionSexualOrientationEnum.ShowSexuality.value)
            }
        }
    }


}