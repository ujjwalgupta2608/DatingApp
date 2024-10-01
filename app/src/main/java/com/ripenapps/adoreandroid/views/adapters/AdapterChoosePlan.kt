package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSubscriptionPlanListingBinding
import com.ripenapps.adoreandroid.models.response_models.planList.Plan

class AdapterChoosePlan(val benefitsList: Plan) : RecyclerView.Adapter<AdapterChoosePlan.ViewHolder>() {
    inner class ViewHolder(val binding:AdapterSubscriptionPlanListingBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_subscription_plan_listing, parent, false))
    }

    override fun getItemCount(): Int {
        return benefitsList.benifits?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.planDescription.text = benefitsList.benifits?.get(position)
    }
}