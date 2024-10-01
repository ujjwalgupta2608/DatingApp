package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.QuestionSelectGenderLayoutBinding
import com.ripenapps.adoreandroid.utils.enums.QuestionInterestedInEnum

class InterestedInVH(private val binding: QuestionSelectGenderLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        isInterestedInMaleSelected: Boolean,
        isEveryOneSelected: Boolean, position: Int, questionThreeItemClick: (Int) -> Unit
    ) {
        if ( isInterestedInMaleSelected) {
            binding.maleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.male_selected))
            binding.femaleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.female_unselected))
            binding.maleText.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.femaleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.maleLayout.background =
                itemView.resources.getDrawable(R.drawable.background_round_corners_theme_color)
            binding.femaleLayout.background =
                itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)
            binding.others.text = itemView.resources.getString(R.string.others)

        } else {
            binding.maleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.male_unselected))
            binding.femaleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.female_selected))
            binding.maleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.femaleText.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.maleLayout.background =
                itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)
            binding.femaleLayout.background =
                itemView.resources.getDrawable(R.drawable.background_round_corners_theme_color)
            binding.others.text = itemView.resources.getString(R.string.others)

        }
        if (isEveryOneSelected) {
            binding.showGenderCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.checked_box))
        } else {
            binding.showGenderCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.unchecked_box))
        }
        binding.others.visibility = View.GONE
        binding.checkedText.text = itemView.resources.getString(R.string.everyone)
        binding.title.text = itemView.resources.getString(R.string.who_are_you_interested_in)
        binding.apply {
            maleLayout.setOnClickListener {
                questionThreeItemClick.invoke(QuestionInterestedInEnum.Male.value)
            }
            femaleLayout.setOnClickListener {
                questionThreeItemClick.invoke(QuestionInterestedInEnum.Female.value)
            }
            showGenderCheck.setOnClickListener {
                questionThreeItemClick.invoke(QuestionInterestedInEnum.EveryOne.value)
            }
        }
    }
}