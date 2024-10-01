package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdaptorAddImageBinding
import com.ripenapps.adoreandroid.utils.enums.AddImagesEnum

class AdapterAddPhoto(
    var imageUriList: ArrayList<String>,
    private val addImagesItemClick: (Int, Int, AdaptorAddImageBinding) -> Unit
) : RecyclerView.Adapter<AdapterAddPhoto.ViewHolder>() {
    inner class ViewHolder(val binding: AdaptorAddImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adaptor_add_image,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (imageUriList.size > position && imageUriList != null && imageUriList[position] != null) {
            Glide.with(holder.binding.root.context).load(imageUriList[position])
                .into(holder.binding.imageView)
            holder.binding.unselectImage.visibility = View.VISIBLE
        }
        holder.binding.apply {
            holder.binding.imageView.setOnClickListener {
                addImagesItemClick.invoke(AddImagesEnum.Add.value, position, holder.binding)
            }
            holder.binding.unselectImage.setOnClickListener {
                addImagesItemClick.invoke(AddImagesEnum.Cancel.value, position, holder.binding)
            }
        }
    }
}