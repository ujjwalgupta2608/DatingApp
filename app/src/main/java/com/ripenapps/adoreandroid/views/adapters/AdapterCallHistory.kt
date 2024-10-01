package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterNotificationBinding
import com.ripenapps.adoreandroid.models.response_models.notificationlist.NotificationData
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.USER_ID
import com.ripenapps.adoreandroid.utils.CommonUtils


class AdapterCallHistory(val list: MutableList<NotificationData>, var profileName:String, var profileImage:String) : RecyclerView.Adapter<AdapterCallHistory.ViewHolder>() {

    inner class ViewHolder(val binding: AdapterNotificationBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_notification, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.callArrow.isVisible=true
        holder.binding.time.isVisible = false

        holder.binding.crossButton.isVisible=true
        val layoutParams: ViewGroup.LayoutParams = holder.binding.crossButton.layoutParams
        layoutParams.width = holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp)
        layoutParams.height = holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp)
        holder.binding.crossButton.layoutParams = layoutParams

        if (list[position].notificationType=="audioCall"){
            holder.binding.crossButton.setImageResource(R.drawable.audio_call_icon_black)
        }else if (list[position].notificationType=="videoCall"){
            holder.binding.crossButton.setImageResource(R.drawable.last_message_video)
        }
        /*if (list[position].message?.contains("missed call") == true&&list[position].sender?._id==Preferences.getStringPreference(holder.itemView.context, USER_ID)){
            holder.binding.name.text = list[position].sender?.name
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .into(holder.binding.profileImage)
            holder.binding.callArrow.setImageResource(R.drawable.missed_call_icon)
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(com.google.android.libraries.places.R.color.quantum_yellow))
            Log.i("TAG", "onBindViewHolder: 1")
        }
        else if (list[position].message?.contains("missed call") == true){
            holder.binding.name.text = list[position].sender?.name
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .into(holder.binding.profileImage)
            holder.binding.callArrow.setImageResource(R.drawable.missed_call_icon)
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(com.google.android.libraries.places.R.color.quantum_yellow))
            Log.i("TAG", "onBindViewHolder: 2")
        }
        else */
        if (list[position].message=="Incoming call"&&list[position].user==Preferences.getStringPreference(holder.itemView.context, USER_ID)){
//            if duration available, show incomming else show as it is
            holder.binding.name.text = profileName
            Glide.with(holder.itemView.context)
                .load(profileImage)
                .into(holder.binding.profileImage)
            if (list[position]?.callStatus==false){
                holder.binding.callArrow.setImageResource(R.drawable.missed_call_icon)
                holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.red_EB4335))
            }else{
                holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
                holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
            }
            Log.i("TAG", "onBindViewHolder: 3")
        }
        else if (list[position].message=="Incoming call"){
            holder.binding.name.text = /*list[position].sender?.name*/profileName
            Glide.with(holder.itemView.context)
                .load(profileImage/*list?.get(position)?.sender?.profileUrl*/)
                .into(holder.binding.profileImage)
            holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
            holder.binding.callArrow.rotation = 180f
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
            if (list[position]?.callStatus==false){
                holder.binding.callArrow.setImageResource(R.drawable.missed_call_icon)
                holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.red_EB4335))
            }else{
                holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
                holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
            }
            Log.i("TAG", "onBindViewHolder: 4")
//            holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
//            holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
//        holder.binding.time.text = CommonUtils.formatSecondsToMinutesAndSeconds(chatMessage.duration)   //duration
        }
        /*else if (list[position].message=="Call rejected"){
            holder.binding.name.text = list[position].sender?.name
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .into(holder.binding.profileImage)
            holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
            holder.binding.callArrow.rotation = 180f
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
            Log.i("TAG", "onBindViewHolder: 5")
        }
        else if (list[position].message=="Call ended"){
            holder.binding.name.text = list[position].sender?.name
            Glide.with(holder.itemView.context)
                .load(list?.get(position)?.sender?.profileUrl)
                .into(holder.binding.profileImage)
            holder.binding.callArrow.setImageResource(R.drawable.received_call_icon)
            holder.binding.callArrow.rotation = 180f
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(com.google.android.libraries.places.R.color.quantum_yellow))
            Log.i("TAG", "onBindViewHolder: 6")
        }
        else{
            Log.i("TAG", "onBindViewHolder: 7")
            holder.binding.callArrow.isVisible=false
//            holder.binding.time.text = ""
            holder.binding.description.setTextColor(holder.itemView.resources.getColor(R.color.grey_boulder))
        }*/
        if (CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")== CommonUtils.getFormattedDateToday("dd MMM yyyy")){
            holder.binding.description.text=
                "${holder.itemView.resources.getString(R.string.today)}, "+CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "hh:mm a")
        } else if(CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")== CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
            holder.binding.description.text="${holder.itemView.resources.getString(R.string.yesterday)}, "+CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "hh:mm a")
        } else{
            holder.binding.description.text=
                CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "dd MMM yyyy")+", "+CommonUtils.convertTimestampToAndroidTime(list[position].createdAt!!, "hh:mm a")
        }

    }
    fun updateList(list:MutableList<NotificationData>){
        Log.i("TAG", "updateList: "+list.size)
        var start = if (this.list.size > 0)
            this.list.size else 0
        this.list.addAll(list)
        notifyItemRangeInserted(start, this.list.size)
    }
    fun getItem(position:Int):NotificationData{
        return list?.get(position)!!
    }
    fun clearList(){
        val size = list?.size
        list?.clear()
        notifyItemRangeRemoved(0, size!!)
    }
}