package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterNotificationBinding
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationData
import com.ripenapps.adoreandroid.preferences.IS_USER_SUBSCRIBED
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.utils.CommonUtils
import jp.wasabeef.glide.transformations.BlurTransformation

class AdapterNotification(
    val getSelectedNotification: (Int) -> Unit,
    val list: MutableList<NotificationData>
) : RecyclerView.Adapter<AdapterNotification.ViewHolder>() {
//    var list:MutableList<NotificationData> = mutableListOf()

    inner class ViewHolder(val binding:AdapterNotificationBinding) :RecyclerView.ViewHolder(binding.root)
    /*init {
//        just get this list from fragment through constructor
        list.addAll(listOf("2", "2", "2"))
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_notification, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.description.text = list[position].message
        if (Preferences.getStringPreference(holder.itemView.context, IS_USER_SUBSCRIBED)=="0"&&list[position].notificationType=="card"){
            holder.binding.name.text = hideName(list[position].title!!)
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .transform(BlurTransformation(25, 4)) // Apply blur transformation
                .into(holder.binding.profileImage)
        }else{
            holder.binding.name.text = list[position].title
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .into(holder.binding.profileImage)
        }
        if (CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")== CommonUtils.getFormattedDateToday("dd MMM yyyy")){
            holder.binding.time.text=
                CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "hh:mm a")
        } else if(CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")== CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
            holder.binding.time.text=holder.itemView.resources.getString(R.string.yesterday)
        } else{
            holder.binding.time.text=
                CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")
        }

        holder.binding.mainLayout.setOnClickListener {
            if (Preferences.getStringPreference(holder.itemView.context, IS_USER_SUBSCRIBED)=="0"&&list[position].notificationType=="card") { }
            else{
                getSelectedNotification(position)
            }
        }
    }
    private fun hideName(input: String): String {
        if (input.length <= 2) {
            return input
        }
        val prefix = input.substring(0, 2)
        val stars = "*".repeat(input.length - 2)
        return prefix + stars
    }
    fun updateList(list:MutableList<NotificationData>){
        var start = if (this.list.size > 0)
            this.list.size else 0
        this.list.addAll(list)
        notifyItemRangeInserted(start, this.list.size)
    }
    fun getItem(position:Int):NotificationData{
        return list?.get(position)!!
    }
    fun clearList(){
        var size = list?.size
        list?.clear()
        notifyItemRangeRemoved(0, size!!)
    }
}