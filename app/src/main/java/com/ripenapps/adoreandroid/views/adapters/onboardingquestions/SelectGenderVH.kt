package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.QuestionSelectGenderLayoutBinding
import com.ripenapps.adoreandroid.utils.enums.QuestionSelectGenderEnum


class SelectGenderVH(
    private val binding: QuestionSelectGenderLayoutBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        selectedGender: Int,
        isShowGenderChecked:Boolean,
        selectedGenderName:String,
        position: Int,
        questionOneItemClick: (Int) -> Unit
    ) {

        binding.others.paintFlags = binding.others.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        if (selectedGender==QuestionSelectGenderEnum.Male.value) {
            //show male selected
            binding.maleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.male_selected))
            binding.femaleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.female_unselected))
            binding.maleText.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.femaleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.maleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_theme_color)
            binding.femaleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)
            binding.others.text = itemView.resources.getString(R.string.others)

        } else if (selectedGender==QuestionSelectGenderEnum.Female.value){
            //show female selected
            binding.maleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.male_unselected))
            binding.femaleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.female_selected))
            binding.maleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.femaleText.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.maleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)
            binding.femaleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_theme_color)
            binding.others.text = itemView.resources.getString(R.string.others)

        } else if (selectedGender==QuestionSelectGenderEnum.Others.value){
            binding.others.text = selectedGenderName
            binding.femaleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.female_unselected))
            binding.femaleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.maleImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.male_unselected))
            binding.maleText.setTextColor(itemView.resources.getColor(R.color.black_mine_shaft))
            binding.maleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)
            binding.femaleLayout.background = itemView.resources.getDrawable(R.drawable.background_round_corners_white_whisper_color)

        }
        if (isShowGenderChecked){
            binding.showGenderCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.checked_box))
        } else {
            binding.showGenderCheck.setImageDrawable(itemView.resources.getDrawable(R.drawable.unchecked_box))
        }
        binding.apply {
            maleLayout.setOnClickListener {
                questionOneItemClick.invoke(QuestionSelectGenderEnum.Male.value)
            }
            femaleLayout.setOnClickListener {
                questionOneItemClick.invoke(QuestionSelectGenderEnum.Female.value)
            }
            others.setOnClickListener {
                questionOneItemClick.invoke(QuestionSelectGenderEnum.Others.value)
            }
            showGenderCheck.setOnClickListener {
                questionOneItemClick.invoke(QuestionSelectGenderEnum.ShowGender.value)
            }
        }
    }
}