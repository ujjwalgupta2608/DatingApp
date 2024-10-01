package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterNotificationBinding
import com.ripenapps.adoreandroid.models.response_models.userSearchListResponse.User

class AdapterSearchUserList(
    val listType: String,
    val getSelected: (String) -> Unit,
    val deleteItem: (Int) -> Unit
) : RecyclerView.Adapter<AdapterSearchUserList.ViewHolder>() {
    var list:MutableList<User> = mutableListOf()
    inner class ViewHolder(val binding: AdapterNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_notification,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.view.isVisible=false
        holder.binding.time.isVisible = false
        holder.binding.crossButton.isVisible = listType=="recentSearch"
        holder.binding.name.text = list[position].name
        holder.binding.description.text = list[position].userName
        Glide.with(holder.itemView.context).load(list?.get(position)?.profileUrl).placeholder(
            R.drawable.placeholder_image).error(R.drawable.placeholder_image).into(holder.binding.profileImage)

        holder.binding.mainLayout.setOnClickListener {
            getSelected(list[position]._id!!)
        }
        holder.binding.crossButton.setOnClickListener {
            deleteItem(position)
        }

    }
    fun updateList(list:MutableList<User>){
        this.list = list
        notifyDataSetChanged()
    }

    fun addItem(user:User){
        list.add(user)
        notifyItemChanged(this.list.size-1)
    }
    fun clearList(){
        val size = this.list.size
        this.list.clear()
        notifyItemRangeRemoved(0, size)
    }
    fun removeItem(position:Int){
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

}