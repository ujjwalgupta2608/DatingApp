package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSearchSpecificUserBinding
import com.ripenapps.adoreandroid.models.OptionsList

class AdapterSearchSpecificUsers(val usersTypeList: ArrayList<OptionsList>, val getUsersType: (Int) -> Unit):RecyclerView.Adapter<AdapterSearchSpecificUsers.ViewHolder>() {
    private var selectedPosition: Int = 0

    inner class ViewHolder(val binding:AdapterSearchSpecificUserBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_search_specific_user, parent, false))
    }

    override fun getItemCount(): Int {
        return usersTypeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = usersTypeList[position]
        holder.binding.name.setOnClickListener {
            if (position != selectedPosition) {
                usersTypeList[position].isOptionSelected = true
                usersTypeList[selectedPosition].isOptionSelected = false
                notifyItemChanged(position)
                notifyItemChanged(selectedPosition)
                selectedPosition = holder.absoluteAdapterPosition
                getUsersType(holder.absoluteAdapterPosition)
            }
        }
    }
}