package com.ripenapps.adoreandroid.views.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterShowMediaViewpagerBinding
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media

class AdapterShowMediaViewPager(val mediaList:MutableList<Media>?, val previousScreen:String): RecyclerView.Adapter<AdapterShowMediaViewPager.ViewHolder>() {
    lateinit var mediaController : MediaController
    inner class ViewHolder(val binding:AdapterShowMediaViewpagerBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_show_media_viewpager, parent, false))
    }

    override fun getItemCount(): Int {
        return mediaList?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mediaList?.get(position)?.type=="image"){
            holder.binding.SingleVideoView.isVisible = false
            holder.binding.image.isVisible = true
            if (previousScreen=="editProfile"||previousScreen=="like"||previousScreen=="home"||previousScreen=="story"||previousScreen=="specificChatProfile"||previousScreen=="showChatMedia"||previousScreen=="search"||previousScreen=="searchUser"){
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.image).placeholder(
                    R.drawable.placeholder_image).error(R.drawable.placeholder_image).into(holder.binding.image)
            }else{
                Glide.with(holder.itemView.context).load(mediaList?.get(position)?.url).placeholder(
                    R.drawable.placeholder_image).error(R.drawable.placeholder_image).into(holder.binding.image)
            }
        }else{
            holder.binding.SingleVideoView.isVisible = true
            holder.binding.image.isVisible = false
            mediaController = MediaController(holder.itemView.context)
            mediaController.setAnchorView(holder.binding.SingleVideoView)
            holder.binding.SingleVideoView.setMediaController(mediaController)
            if (previousScreen=="editProfile"||previousScreen=="like"||previousScreen=="home"||previousScreen=="story"||previousScreen=="specificChatProfile"||previousScreen=="showChatMedia"||previousScreen=="search"||previousScreen=="searchUser"){
                holder.binding.SingleVideoView.setVideoURI(Uri.parse(mediaList?.get(position)?.video))
            }else{
                holder.binding.SingleVideoView.setVideoURI(Uri.parse(mediaList?.get(position)?.url))
            }
            holder.binding.SingleVideoView.requestFocus()
            holder.binding.SingleVideoView.setOnPreparedListener {
                holder.binding.SingleVideoView.start() }
        }
    }
}