package com.ripenapps.adoreandroid.views.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterUserInLikeBinding
import com.ripenapps.adoreandroid.models.response_models.connectionlist.Listing
import com.ripenapps.adoreandroid.preferences.IS_USER_SUBSCRIBED
import com.ripenapps.adoreandroid.preferences.Preferences
import jp.wasabeef.glide.transformations.BlurTransformation

class AdapterUsersListInLike(
    var listing: MutableList<Listing>?,
    private val getSelectedUser: (Int, String) -> Unit,
    private val openSubscriptionPopup:()->Unit,
    var usersType: Int
) :
    RecyclerView.Adapter<AdapterUsersListInLike.ViewHolder>() {
    inner class ViewHolder(val binding: AdapterUserInLikeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_user_in_like, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return listing?.size!!
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = getFirstWord(listing?.get(position)?.userInfo?.name)
        holder.binding.age.text = ", "+listing?.get(position)?.userInfo?.age.toString()
        holder.binding.onlineImage.isVisible = listing?.get(position)?.userInfo?.is_online=="1"
        if (!listing?.get(position)?.userInfo?.city.isNullOrEmpty()) {
            holder.binding.location.text = listing?.get(position)?.userInfo?.city
            if (listing?.get(position)?.distance!=null){
                holder.binding.location.text = listing?.get(position)?.userInfo?.city+", "+getCharactersBeforeDot(listing?.get(position)?.distance.toString())+" km"
            }
        }
        if (listing?.get(position)?.userStatus=="request" && Preferences.getStringPreference(holder.itemView.context, IS_USER_SUBSCRIBED)=="0") {
            Glide.with(holder.itemView.context)
                .load(listing?.get(position)?.userInfo?.profile)
                .transform(BlurTransformation(25, 4)) // Apply blur transformation
                .into(holder.binding.blurImage)
            holder.binding.blurCardView.isVisible = true
        } else {
            Glide.with(holder.itemView.context)
                .load(listing?.get(position)?.userInfo?.profile)
                .into(holder.binding.userImage)
            holder.binding.blurCardView.isVisible = false
        }


        holder.binding.mainLayout.setOnClickListener {

            if (/*usersType == 3*/listing?.get(position)?.userStatus=="request"&&Preferences.getStringPreference(holder.itemView.context, IS_USER_SUBSCRIBED)=="0") {
                openSubscriptionPopup()
            } else {
                getSelectedUser(position, listing?.get(position)?.userInfo?._id.toString())
            }
        }
    }

    fun updateUsersList(listing: MutableList<Listing>?, usersType:Int) {
//        updateList
        this.usersType=usersType
        this.listing = listing
        notifyDataSetChanged()
    }
    fun getFirstWord(input: String?): String {
        // Check if input string is null or empty
        if (input.isNullOrEmpty()) {
            return ""
        }

        // Split the input string by whitespace
        val words = input.trim().split("\\s+".toRegex())

        // Check if there are any words after trimming
        return if (words.isNotEmpty()) {
            words[0]
        } else {
            "" // Return an empty string if no words are found
        }
    }
    fun getTwoCharactersAfterDot(input: String): String? {
        val dotIndex = input.indexOf('.')
        if (dotIndex != -1 && dotIndex + 3 <= input.length) {
            return input.substring(0, dotIndex + 3)
        }
        return input
    }
    fun getCharactersBeforeDot(input: String): String {
        val dotIndex = input.indexOf('.')
        if (dotIndex != -1) {
            return input.substring(0, dotIndex)
        }
        return input
    }
}