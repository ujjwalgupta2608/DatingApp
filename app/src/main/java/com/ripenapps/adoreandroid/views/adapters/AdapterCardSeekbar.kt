package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterCardSeekbarBinding

class AdapterCardSeekbar(val listSize:Int): RecyclerView.Adapter<AdapterCardSeekbar.ViewHolder>() {
    var selectedPosition = 0
    private var itemWidth: Int = 0

    fun setItemWidth(width: Int) {
        itemWidth = width
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding:AdapterCardSeekbarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_card_seekbar, parent, false))
    }

    override fun getItemCount(): Int {
        return listSize
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = itemWidth
        holder.itemView.layoutParams = layoutParams
        if (position==selectedPosition){
            holder.binding.seekBarView.setBackgroundColor(holder.itemView.resources.getColor(R.color.white))
//            holder.binding.seekBarView.background = holder.itemView.resources.getDrawable(R.drawable.background_round_corners_theme_color)
        } else{
            holder.binding.seekBarView.setBackgroundColor(holder.itemView.resources.getColor(R.color.white_20))
//            holder.binding.seekBarView.background = holder.itemView.resources.getDrawable(R.drawable.background_corners_18dp)
        }
    }
    fun updatePosition(selectedPosition: Int){
        var prevSelected = this.selectedPosition
        this.selectedPosition = selectedPosition
        notifyItemChanged(prevSelected)
        notifyItemChanged(selectedPosition)
    }
}