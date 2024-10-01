package com.ripenapps.adoreandroid.views.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterStoriesRecyclerBinding
import com.ripenapps.adoreandroid.models.response_models.storyListing.StoryListing

class AdapterStoriesRecycler(
    val selector: StorySelector,
    var isMediaInMyStory: Boolean,
    var myStoryImagePath: String,
    val storyListing: List<StoryListing>,
    val screenName: String
) : RecyclerView.Adapter<AdapterStoriesRecycler.ViewHolder>() {


    inner class ViewHolder(val binding: AdapterStoriesRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_stories_recycler,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return storyListing.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (screenName == "home") {
            holder.binding.onlineImage.isVisible = false
            if (position == 0) {
                holder.binding.otherStoryCardview.isVisible = false
                holder.binding.myStoryLayout.isVisible = true
                if (isMediaInMyStory) {
                    holder.binding.plusCenter.isVisible = false
                    holder.binding.plusBottomRight.isVisible = true
                    holder.binding.myStoryCardView.strokeWidth = 6
                    holder.binding.myStoryCardView.strokeColor =
                        holder.itemView.resources.getColor(R.color.theme)
                    Glide.with(holder.binding.root.context)
                        .load(myStoryImagePath).placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(holder.binding.myStoryImageView)
                } else {
                    Glide.with(holder.binding.root.context)
                        .load(myStoryImagePath)
                        .centerCrop()
                        .into(object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                                // Implementation if needed
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                // Set the loaded image as background
                                holder.binding.myStoryImageView.background = resource
                            }
                        })
                    holder.binding.plusCenter.isVisible = true
                    holder.binding.plusBottomRight.isVisible = false
                    holder.binding.myStoryCardView.strokeWidth = 0
                }
            } else {
                holder.binding.otherStoryCardview.isVisible = true
                holder.binding.myStoryLayout.isVisible = false
                Glide.with(holder.binding.root.context).load(storyListing[position - 1].profile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                    .skipMemoryCache(true)
                    .into(holder.binding.otherStoryImageview)
                holder.binding.name.text = storyListing[position - 1].userName
            }
        } else if (screenName == "chat") {
            holder.binding.onlineImage.isVisible = false
            holder.binding.otherStoryCardview.isVisible = true
            holder.binding.myStoryLayout.isVisible = false
            holder.binding.name.setTextColor(holder.itemView.resources.getColor(R.color.white))
            if (position == 0) {
                Glide.with(holder.binding.root.context).load(myStoryImagePath)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    /*.diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                    .skipMemoryCache(true)*/
                    .into(holder.binding.otherStoryImageview)
                holder.binding.name.text = holder.itemView.resources.getString(R.string.you)
            } else {
                holder.binding.onlineImage.isVisible = storyListing[position - 1].isOnline == "1"
                Glide.with(holder.binding.root.context).load(storyListing[position - 1].profile)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    /*.diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                    .skipMemoryCache(true)*/
                    .into(holder.binding.otherStoryImageview)
                holder.binding.name.text = storyListing[position - 1].userName
            }
        }
        holder.binding.storyLayout.setOnClickListener {
            if (screenName == "home") {
                if (position == 0)
                    selector.getSelectedStory(position, "0")
                else
                    selector.getSelectedStory(position, storyListing[position - 1]._id!!)
            } else if (screenName == "chat") {
                if (position != 0) {
                    selector.getSelectedStory(position, storyListing[position - 1]._id!!)
                }
            }
        }
        holder.binding.plusBottomRight.setOnClickListener {
            if (screenName == "home") {
                selector.uploadStory()
            }
        }
    }

    interface StorySelector {
        fun getSelectedStory(position: Int, userId: String)
        fun uploadStory()

    }

}