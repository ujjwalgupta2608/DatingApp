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
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media

class AdapterMyProfileMedia(
    var mediaList: MutableList<Media>?,
    val selector: ProfileMediaSelector,
    val context: Context
) : RecyclerView.Adapter<AdapterMyProfileMedia.ViewHolder>() {
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
                    mediaList?.map { media: Media -> media.isSelected = false }
                    mediaList?.get(position)?.isSelected = true
                    selector.setLongPress(true)
                    notifyDataSetChanged()
                }

                override fun onSingleTapUp(p0: MotionEvent): Boolean {
                    if (isLongPressActive) {
                        if (mediaList?.get(position)?.isSelected == false) {
                            mediaList?.get(position)?.isSelected = true
                            notifyItemChanged(holder.absoluteAdapterPosition)
                        } else {
                            mediaList?.get(position)?.isSelected = false
                            notifyItemChanged(holder.absoluteAdapterPosition)
                        }
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
            if (mediaList?.get(position)?.isSelected == false) {
                holder.binding.selectedImage.setImageResource(R.drawable.media_selector_icon)
            } else {
                holder.binding.selectedImage.setImageResource(R.drawable.media_selected)
            }
        } else {
            mediaList?.get(position)?.isSelected = false
            holder.binding.selectedImage.isVisible = false
        }
        if (mediaList?.get(position)?.type == "image") {
            holder.binding.videoIcon.isVisible = false
            Glide.with(holder.itemView.context).load(mediaList?.get(position)?.image)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image).into(holder.binding.galleryImageView)
        } else {
            holder.binding.videoIcon.isVisible = true
            Glide.with(holder.itemView.context).load(mediaList?.get(position)?.thumbnail)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image).into(holder.binding.galleryImageView)
        }
    }

    fun inactiveLongPress() {
        isLongPressActive = false
        notifyDataSetChanged()
    }

    fun getMediaListToDelete(): MutableList<Media>? {
        var list: MutableList<Media>? = mutableListOf()
        mediaList?.forEach { media: Media ->
            if (media?.isSelected == true) {
                list?.add(media)
            }
        }
        return list
    }

    interface ProfileMediaSelector {
        fun showMedia(position: Int)
        fun getSelectedMediaPosition(position: Int)
        fun setLongPress(status: Boolean)
    }
}