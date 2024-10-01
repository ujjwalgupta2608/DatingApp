package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdaptorLookingForBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData

class AdapterLookingFor(var list: MutableList<GenderData>, private val lookingForAdapterClick:(Int)->Unit, selectedLookingForPosition:Int) :
    RecyclerView.Adapter<AdapterLookingFor.ViewHolder>() {
    var selectedPosition = selectedLookingForPosition

    inner class ViewHolder(var binding: AdaptorLookingForBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adaptor_looking_for,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position].isSelected == true){
            selectedPosition = holder.absoluteAdapterPosition
        }
        holder.binding.model = list[position]
        holder.binding.mainLayout.setOnClickListener {
            if (position != selectedPosition) {
                list[position].isSelected = true
                if (selectedPosition != -1)
                    list[selectedPosition].isSelected = false
                notifyItemChanged(holder.absoluteAdapterPosition)
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.absoluteAdapterPosition
                lookingForAdapterClick(selectedPosition)
            } else {
                list[selectedPosition].isSelected = false
                notifyItemChanged(selectedPosition)
                selectedPosition = -1
                lookingForAdapterClick(selectedPosition)
            }
        }
    }
}