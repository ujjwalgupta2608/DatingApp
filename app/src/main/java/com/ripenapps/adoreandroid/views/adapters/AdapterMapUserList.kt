package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterMapUserlistBinding
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.User

class AdapterMapUserList(var list: MutableList<User>, val getSelectedUser: (String) -> Unit) : RecyclerView.Adapter<AdapterMapUserList.ViewHolder>() {
    inner class ViewHolder(val binding:AdapterMapUserlistBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_map_userlist, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].name
        if (list[position]?.city.isNullOrEmpty()){
            holder.binding.description.isVisible=false
        }else{
            holder.binding.description.isVisible=true
            holder.binding.description.text = list[position].city
        }
        Glide.with(holder.itemView.context).load(list?.get(position)?.profileUrl)
            .into(holder.binding.profileImage)
        holder.binding.mainLayout.setOnClickListener {
            getSelectedUser(list[position]?._id!!)
        }
    }
}