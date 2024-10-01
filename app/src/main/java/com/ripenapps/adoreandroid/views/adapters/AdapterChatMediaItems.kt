package com.ripenapps.adoreandroid.views.adapters

import android.content.Context
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterMyProfileMediaBinding
import com.ripenapps.adoreandroid.models.response_models.chatMediaList.Result

class AdapterChatMediaItems(
    var mediaList: MutableList<Result>?,
    val selector: AdapterMyProfileMedia.ProfileMediaSelector,
    val context: Context
) : RecyclerView.Adapter<AdapterChatMediaItems.ViewHolder>() {
    var isLongPressActive = false
    inner class ViewHolder(val binding: AdapterMyProfileMediaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_my_profile_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mediaList?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    isLongPressActive = true
                    mediaList?.map { media: Result -> media.media?.get(0)?.isSelected = false }
                    mediaList?.get(position)?.media?.get(0)?.isSelected = true
                    selector.setLongPress(true)
                    notifyDataSetChanged()
                    selector.getSelectedMediaPosition(position)
                }

                override fun onSingleTapUp(p0: MotionEvent): Boolean {
                    if (isLongPressActive) {
                        if (mediaList?.get(position)?.media?.get(0)?.isSelected == false) {
                            mediaList?.get(position)?.media?.get(0)?.isSelected = true
                            notifyItemChanged(holder.absoluteAdapterPosition)
                        } else {
                            mediaList?.get(position)?.media?.get(0)?.isSelected = false
                            notifyItemChanged(holder.absoluteAdapterPosition)
                        }
                        selector.getSelectedMediaPosition(position)
                    } else {
                        selector.showMedia(holder.absoluteAdapterPosition)
                    }
                    return true
                }
            })
        holder.binding.root.setOnTouchListener { _, event ->
            gestureDetector?.onTouchEvent(event)
            true
        }
        if (isLongPressActive) {
            holder.binding.selectedImage.isVisible = true
            if (mediaList?.get(position)?.media?.get(0)?.isSelected == false) {
                holder.binding.selectedImage.setImageResource(R.drawable.media_selector_icon)
            } else {
                holder.binding.selectedImage.setImageResource(R.drawable.media_selected)
            }
        } else {
            mediaList?.get(position)?.media?.get(0)?.isSelected = false
            holder.binding.selectedImage.isVisible = false
        }
        if (mediaList?.get(position)?.media?.get(0)?.type == "image") {
            holder.binding.videoIcon.isVisible = false
            Glide.with(holder.itemView.context).load(mediaList?.get(position)?.media?.get(0)?.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image).into(holder.binding.galleryImageView)
        } else {
            holder.binding.videoIcon.isVisible = true
            Glide.with(holder.itemView.context).load(mediaList?.get(position)?.media?.get(0)?.thumbnail)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image).into(holder.binding.galleryImageView)
        }
    }
    fun inactiveLongPress() {
        isLongPressActive = false
        notifyDataSetChanged()
    }

    fun getMediaListToDelete(): MutableList<String> {
        var list: MutableList<String> = mutableListOf()
        mediaList?.forEach { media: Result ->
            if (media?.media?.get(0)?.isSelected == true) {
                list?.add(media?.media?.get(0)?._id!!)
            }
        }
        return list
    }
}