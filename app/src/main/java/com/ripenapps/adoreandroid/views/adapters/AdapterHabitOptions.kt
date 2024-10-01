package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterHabitsAndOtherDetailsBinding
import com.ripenapps.adoreandroid.models.OptionsList

class AdapterHabitOptions(var list: MutableList<OptionsList>, var selectedOptionPosition:Int, var currentQuestionPosition:Int, private val setSelectedOptionPosition: (Int, Int) -> Unit) :
    RecyclerView.Adapter<AdapterHabitOptions.ViewHolder>() {
//        var selectedOptionPosition = -1

    inner class ViewHolder(binding: AdapterHabitsAndOtherDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_habits_and_other_details,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.name.setOnClickListener {
            if (position != selectedOptionPosition) {
                list[position].isOptionSelected = true
                if (selectedOptionPosition != -1)
                    list[selectedOptionPosition].isOptionSelected = false
                notifyItemChanged(holder.absoluteAdapterPosition)
                notifyItemChanged(selectedOptionPosition)
                selectedOptionPosition = holder.absoluteAdapterPosition
                setSelectedOptionPosition(currentQuestionPosition, holder.absoluteAdapterPosition)
            } else {
                list[selectedOptionPosition].isOptionSelected = false
                notifyItemChanged(selectedOptionPosition)
                selectedOptionPosition = -1
                setSelectedOptionPosition(currentQuestionPosition, -1)
            }
        }
    }
}