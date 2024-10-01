package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSelectInterestsBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData

class AdapterSelectInterests(list: List<InterestData>, private var selectInterestsAdapterClick:(List<InterestData>)->Unit) :
    RecyclerView.Adapter<AdapterSelectInterests.ViewHolder>() {
    var list = list
    private var selectedItems = 0


    inner class ViewHolder(binding: AdapterSelectInterestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding: AdapterSelectInterestsBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_select_interests,
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
        Glide.with(holder.binding.root.context).load(list[position].iconeUrl)
            .into(holder.binding.imageView)
        holder.binding.mainLayout.setOnClickListener {
            if (list[position].isSelected){
                selectedItems--
                list[position].isSelected = false
                selectInterestsAdapterClick(list)
                notifyItemChanged(position)
            }else{
                if (selectedItems<5){
                    selectedItems++
                    list[position].isSelected = true
                    selectInterestsAdapterClick(list)
                    notifyItemChanged(position)
                }
            }

        }
    }
}