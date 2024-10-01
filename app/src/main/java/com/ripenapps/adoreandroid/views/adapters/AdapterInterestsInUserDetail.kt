package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSelectInterestsBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData

class AdapterInterestsInUserDetail(val topInterests: MutableList<InterestData>? ) :
    RecyclerView.Adapter<AdapterInterestsInUserDetail.ViewHolder>() {

    inner class ViewHolder(val binding: AdapterSelectInterestsBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        return topInterests?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        topInterests?.get(position)?.isSelected = false
        Glide.with(holder.binding.root.context).load(topInterests?.get(position)?.icon)
            .into(holder.binding.imageView)
        holder.binding.model = topInterests?.get(position)
    }
}