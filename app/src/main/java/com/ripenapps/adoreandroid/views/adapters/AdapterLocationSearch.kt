package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterLocationSearchBinding
import com.ripenapps.adoreandroid.models.static_models.SavedAddressModel

class AdapterLocationSearch(val getSelected: (SavedAddressModel) -> Unit) : RecyclerView.Adapter<AdapterLocationSearch.ViewHolder>() {
    var list:MutableList<SavedAddressModel> = mutableListOf()

    class ViewHolder(val binding: AdapterLocationSearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_location_search,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.locationPoint.text = list[position].locality
        holder.binding.locationFull.text = list[position].fullAddress
        holder.binding.mainLayout.setOnClickListener {
            getSelected(list[position])
        }
    }

    fun updateList(list:MutableList<SavedAddressModel>){
        this.list = list
        notifyDataSetChanged()
    }
    fun addItem(user: SavedAddressModel){
        list.add(user)
        notifyItemChanged(this.list.size-1)
    }
}