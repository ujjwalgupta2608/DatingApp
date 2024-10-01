package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterShowMediaBinding
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media

class AdapterShowMediaList(
    var getPosition: (Int) -> Unit,
    val mediaList: MutableList<Media>?,
    selectedPosition: Int,
    val previousScreen: String
) :
    RecyclerView.Adapter<AdapterShowMediaList.ViewHolder>() {
    var selectedPosition = selectedPosition
    var previousSelected = 0

    inner class ViewHolder(val binding: AdapterShowMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_show_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mediaList?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (previousScreen == "like" || previousScreen == "home"||previousScreen=="story"||previousScreen=="specificChatProfile"||previousScreen=="search"||previousScreen=="searchUser") {
            if (mediaList?.get(position)?.type == "image") {
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.image)
                    .into(holder.binding.image)
            } else {
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.videoThumbnail)
                    .into(holder.binding.image)
            }
        } else if (previousScreen == "editProfile"||previousScreen=="showChatMedia") {
            if (mediaList?.get(position)?.type == "image") {
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.image)
                    .into(holder.binding.image)
            } else {
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.thumbnail)
                    .into(holder.binding.image)
            }
//            Glide.with(holder.itemView.context).load(mediaList?.get(position)?.url).into(holder.binding.image)
        }
        if (position == selectedPosition) {
            holder.binding.cardView.strokeColor = holder.itemView.resources.getColor(R.color.theme)
            holder.binding.cardView.strokeWidth = 1
        } else {
            holder.binding.cardView.strokeColor =
                holder.itemView.resources.getColor(R.color.transparent)
            holder.binding.cardView.strokeWidth = 0
        }
        holder.binding.image.setOnClickListener {
            if (position != selectedPosition) {
                getPosition(position)
                previousSelected = selectedPosition
                notifyItemChanged(position)
                notifyItemChanged(selectedPosition)
                selectedPosition = position
            }
        }
    }

    fun updatePosition(position: Int) {
        previousSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousSelected)
        notifyItemChanged(selectedPosition)
    }
}