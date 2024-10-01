package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSelectGenderBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData

class AdapterSelectGender(gendersList: MutableList<GenderData>, var selectedPosition:Int,  private val selectedGenderPosition: (Int) -> Unit) :
    RecyclerView.Adapter<AdapterSelectGender.ViewHolder>() {
    var gendersList: MutableList<GenderData> = gendersList

    inner class ViewHolder(itemView: AdapterSelectGenderBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: AdapterSelectGenderBinding

        init {
            binding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_select_gender,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return this.gendersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        gendersList[position].isSelected = holder.absoluteAdapterPosition == selectedPosition
        holder.binding.model = this.gendersList[position]
        holder.binding.name.setOnClickListener {
            if (position != selectedPosition) {
                gendersList[position].isSelected = true
                if (selectedPosition != -1)
                    gendersList[selectedPosition].isSelected = false
                notifyItemChanged(position)
                notifyItemChanged(selectedPosition)
                selectedPosition = position
                selectedGenderPosition(holder.absoluteAdapterPosition)
            }
        }

    }

}