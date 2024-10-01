package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSexualOrientationBinding
import com.ripenapps.adoreandroid.models.static_models.SelectGenderModel

class AdapterDeleteReasons(
    val list: MutableList<SelectGenderModel>,
    val getSelectedPosition: (Int) -> Unit
): RecyclerView.Adapter<AdapterDeleteReasons.ViewHolder>() {
    var selectedPosition=0
    inner class ViewHolder(val binding:AdapterSexualOrientationBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_sexual_orientation, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.mainLayout.setOnClickListener {
            if (position != selectedPosition) {
                list[position].isSelected = true
//                if (selectedPosition != -1)
                    list[selectedPosition].isSelected = false
                notifyItemChanged(position)
                notifyItemChanged(selectedPosition)
                selectedPosition = position
                getSelectedPosition(position)
            }
        }
    }
}