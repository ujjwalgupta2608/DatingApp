package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterStoryViewsItemBinding
import com.ripenapps.adoreandroid.models.response_models.storyViewers.StoryViewersResponse

class AdaptorStoryViews(
    val getSelectedViewer: (String) -> Unit,
    val storyViewersList: StoryViewersResponse
) :
    RecyclerView.Adapter<AdaptorStoryViews.ViewHolder>() {
    inner class ViewHolder(val binding: AdapterStoryViewsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_story_views_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return storyViewersList.data.viewersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = storyViewersList.data.viewersList[position].viewers
        Glide.with(holder.itemView.context)
            .load(storyViewersList.data.viewersList[position].viewers.profileUrl)
            .into(holder.binding.imageOfViewer)
        holder.binding.mainLayout.setOnClickListener {
            getSelectedViewer(storyViewersList.data.viewersList[position].viewers._id)
        }

    }
}