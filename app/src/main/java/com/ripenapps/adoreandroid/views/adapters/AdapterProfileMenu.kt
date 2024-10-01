package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterProfileMenuBinding

class AdapterProfileMenu(
    val menuTitleList: List<String>,
    val menuIconList: List<Int>,
    val screen:String,
    val getMenuItemPosition: (Int) -> Unit
): RecyclerView.Adapter<AdapterProfileMenu.ViewHolder>() {
    inner class ViewHolder(val binding:AdapterProfileMenuBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_profile_menu, parent, false))
    }

    override fun getItemCount(): Int {
        return menuTitleList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.titleText = menuTitleList[position]
        holder.binding.startIcon.setImageResource(menuIconList[position])
        if(position==menuTitleList.size-1){
            holder.binding.view.isVisible = false
        }
        if (screen=="setting"){
            if (position==menuTitleList.size-1){
                holder.binding.menuText.setTextColor(holder.itemView.resources.getColor(R.color.red_EB4335))
                holder.binding.menuText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.next_icon_red, 0);

            }
            /*else if (position==menuTitleList.size-1){
                holder.binding.menuText.setTextColor(holder.itemView.resources.getColor(R.color.red_EB4335))
                holder.binding.menuText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }*/
        } else if (screen=="profile"){
            if (position==menuTitleList.size-1){
                holder.binding.menuText.setTextColor(holder.itemView.resources.getColor(R.color.red_EB4335))
                holder.binding.menuText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            }
        }
        holder.binding.menuItem.setOnClickListener {
            getMenuItemPosition(position)
        }
    }
}