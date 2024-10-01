package com.ripenapps.adoreandroid.views.adapters.onboardingquestions

import android.os.LocaleList
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ripenapps.adoreandroid.databinding.LayoutCompleteProfileBinding
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE
import java.util.Locale

class CompleteProfileVH(val binding:LayoutCompleteProfileBinding):RecyclerView.ViewHolder(binding.root) {

    fun bind(imageUri: String, position:Int, completeProfileClick:(Int, String, String)->Unit){
        Log.i("TAG", "bind: $imageUri")
//        Glide.with(binding.root.context).load(imageUri)
//            .into(binding.image)
        binding.userBio.imeHintLocales = LocaleList(Locale(Preferences.getStringPreference(binding.root.context, SELECTED_LANGUAGE_CODE)))

        binding.userID.addTextChangedListener {
            completeProfileClick.invoke(1, binding.userID.text.toString(), binding.userBio.text.toString())
        }
        binding.userBio.addTextChangedListener {
            completeProfileClick.invoke(1, binding.userID.text.toString(), binding.userBio.text.toString())
        }
        Glide.with(binding.root.context).load(imageUri).diskCacheStrategy(
            DiskCacheStrategy.NONE).skipMemoryCache(true)
            .centerCrop()
            .into(binding.image)
        binding.apply {
            binding.image.setOnClickListener {
                completeProfileClick.invoke(0, binding.userID.text.toString(), binding.userBio.text.toString())
            }
        }

    }
}