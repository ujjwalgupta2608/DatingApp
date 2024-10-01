package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterHabitsListBinding
import com.ripenapps.adoreandroid.models.StepSevenEightModel
import com.google.android.flexbox.FlexboxLayoutManager

class AdaptorOtherDetailsList(var list: MutableList<StepSevenEightModel>) :
    RecyclerView.Adapter<AdaptorOtherDetailsList.ViewHolder>() {
    private var selectedPosition = -1
    private lateinit var layoutManager: FlexboxLayoutManager
    private lateinit var optionsAdapter: AdapterHabitOptions

    inner class ViewHolder(val binding: AdapterHabitsListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdaptorOtherDetailsList.ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_habits_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        layoutManager = FlexboxLayoutManager(holder.binding.root.context)
        optionsAdapter =
            AdapterHabitOptions(
                list[position].optionsList!!,
                list[position].selectedOptionPosition!!,
                holder.absoluteAdapterPosition,
                ::setSelectedOptionPosition
            )
        holder.binding.optionsRecycler.layoutManager = layoutManager
        holder.binding.model = list[position]
        (holder.binding.optionsRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        holder.binding.optionsRecycler.adapter = optionsAdapter

        if (list[position].isSelected==true){
            selectedPosition = holder.absoluteAdapterPosition
        }
        holder.binding.questionLayout.setOnClickListener {
            if (holder.absoluteAdapterPosition != selectedPosition) {
                list[position].isSelected = true
                if (selectedPosition != -1)
                    list[selectedPosition].isSelected = false
                notifyItemChanged(position)
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.absoluteAdapterPosition
            } else {
                list[selectedPosition].isSelected = false
                notifyItemChanged(selectedPosition)
                selectedPosition = -1
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun setSelectedOptionPosition(questionPosition: Int, optionPosition: Int) {
        list[questionPosition].selectedOptionPosition = optionPosition
    }

}