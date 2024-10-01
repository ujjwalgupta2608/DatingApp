package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.databinding.AdaptorAddImageBinding
import com.ripenapps.adoreandroid.databinding.QuestionAddPhotosLayoutBinding
import com.ripenapps.adoreandroid.views.adapters.AdapterAddPhoto

class AddPhotosVH(val binding: QuestionAddPhotosLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int, imageUriList:ArrayList<String>, addImagesItemClick: (Int, Int, AdaptorAddImageBinding) -> Unit) {
        binding.addImageRecycler.adapter = AdapterAddPhoto( imageUriList) { newStr, pos, binding ->
            addImagesItemClick.invoke(newStr, pos, binding)
        }
    }

}