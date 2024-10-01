package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterUserDetailGalleryBinding
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media

class AdapterGalleryInUserDetail(val media: MutableList<Media>?,val previousScreen:String, val getSelectedPosition: (Int) -> Unit) :
    RecyclerView.Adapter<AdapterGalleryInUserDetail.ViewHolder>() {
    inner class ViewHolder(val binding: AdapterUserDetailGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_user_detail_gallery,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (media?.size!! > 4) 4 else media?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (media?.get(position)?.type == "image") {
            if (previousScreen=="like"||previousScreen=="home"||previousScreen=="story"||previousScreen=="specificChatProfile"||previousScreen=="search"||previousScreen=="searchUser"){
                Glide.with(holder.itemView.context).load(media[position]?.image)
                    .into(holder.binding.imageView)
            }else {
                Glide.with(holder.itemView.context).load(media[position]?.url)
                    .into(holder.binding.imageView)
            }
        } else {
            if (previousScreen=="like"||previousScreen=="home"||previousScreen=="story"||previousScreen=="specificChatProfile"||previousScreen=="search"||previousScreen=="searchUser"){
                Glide.with(holder.itemView.context).load(media?.get(position)?.videoThumbnail)
                    .into(holder.binding.imageView)
            }else {
                Glide.with(holder.itemView.context).load(media?.get(position)?.url)
                    .into(holder.binding.imageView)
            }

        }
        holder.binding.imageView.setOnClickListener {
            getSelectedPosition(position)
        }
    }
}