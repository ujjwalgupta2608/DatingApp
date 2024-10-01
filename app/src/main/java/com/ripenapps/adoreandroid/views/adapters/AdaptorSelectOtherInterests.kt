package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterRoundedCornerTextviewBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData

class AdaptorSelectOtherInterests(list: List<InterestData>, private var otherInterestsAdapterClick:(List<InterestData>)->Unit) :
    RecyclerView.Adapter<AdaptorSelectOtherInterests.ViewHolder>() {
    var list = list

    inner class ViewHolder(binding: AdapterRoundedCornerTextviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_rounded_corner_textview,
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
            if (list[position].isSelected) {
                list[position].isSelected = false
                otherInterestsAdapterClick(list)
                notifyItemChanged(position)
            } else {
                list[position].isSelected = true
                otherInterestsAdapterClick(list)
                notifyItemChanged(position)

            }
        }
    }
}