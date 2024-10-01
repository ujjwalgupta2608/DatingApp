package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSexualOrientationBinding
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel

class AdapterSexualOrientation(list: MutableList<SelectGenderModel>, private val sexualOrientationAdapterClick:(Int)->Unit, var sexualOrientationSelectedPostion:Int):RecyclerView.Adapter<AdapterSexualOrientation.ViewHolder>() {
    var list = list

    inner class ViewHolder(itemView:AdapterSexualOrientationBinding):RecyclerView.ViewHolder(itemView.root){
        var binding:AdapterSexualOrientationBinding
        init {
            binding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_sexual_orientation, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.mainLayout.setOnClickListener {
            if (position != sexualOrientationSelectedPostion) {
                list[position].isSelected = true
                if (sexualOrientationSelectedPostion != -1)
                    list[sexualOrientationSelectedPostion].isSelected = false
                notifyItemChanged(position)
                notifyItemChanged(sexualOrientationSelectedPostion)
                sexualOrientationSelectedPostion = position
            }
            sexualOrientationAdapterClick(position)
        }
    }
}