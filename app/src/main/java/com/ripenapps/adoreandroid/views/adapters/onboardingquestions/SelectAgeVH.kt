package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.QuestionSelectAgeLayoutBinding

class SelectAgeVH(val binding: QuestionSelectAgeLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, selectedAge:Int, SelectAgeItemClick: (String) -> Unit) {
        binding.numberPicker.setShownItemCount(7)
        binding.numberPicker.setItemsIntRange(18, 100)
        binding.numberPicker.setSelectedTextSize(150F)
        binding.numberPicker.setSelectorColor(binding.root.context.getColor(R.color.theme))
        binding.numberPicker.setSelectedTextColor(binding.root.context.getColor(R.color.theme))
        binding.numberPicker.setTextColor(binding.root.context.getColor(R.color.grey_boulder))
        binding.numberPicker.setTextBold(true)
        binding.numberPicker.setSelectorLineWidth(5F)
        binding.numberPicker.value = selectedAge
        binding.numberPicker.addOnValueChangedListener {
            SelectAgeItemClick(binding.numberPicker.selectedItemText)
        }
    }
}