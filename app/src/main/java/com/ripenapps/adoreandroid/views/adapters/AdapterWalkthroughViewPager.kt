package com.ripenapps.adoreandroid.views.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdaptorWalkthroughBinding
import com.ripenapps.adoreandroid.models.static_models.ViewPagerWalkthroughModel

class AdapterWalkthroughViewPager(var onSkip:() -> Unit,var context: Context, var list:ArrayList<ViewPagerWalkthroughModel>) :
    RecyclerView.Adapter<AdapterWalkthroughViewPager.ViewHolder>() {
    inner class ViewHolder(itemView:AdaptorWalkthroughBinding): RecyclerView.ViewHolder(itemView.root) {
        var binding: AdaptorWalkthroughBinding

        init {
            binding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adaptor_walkthrough, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.data = list[position]
        when (position) {
            0 -> {
                holder.binding.onboardingImage.setImageResource(R.mipmap.onboarding_image_one)
            }
            1 -> {
                holder.binding.onboardingImage.setImageResource(R.mipmap.onboarding_image_two)
            }
            2 -> {
                holder.binding.onboardingImage.setImageResource(R.mipmap.onboarding_image_three)
            }
            3 -> {
                holder.binding.onboardingImage.setImageResource(R.mipmap.onboarding_screen_four)
            }
            4 -> {
                holder.binding.onboardingImage.setImageResource(R.mipmap.onboarding_screen_five)
            }
        }
        var title = "<font color=#6D53F4>${holder.itemView.resources.getString(R.string.meeting_new_people)}</font> <font color=#242424>${holder.itemView.resources.getString(R.string.in_your_area)}</font>"
        holder.binding.title.text = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY)
        holder.binding.skipFromWalkthrough.setOnClickListener {
            onSkip()
        }
    }
}