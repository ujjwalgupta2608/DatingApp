package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterChatsListingBinding
import com.ripenapps.adoreandroid.models.response_models.navigationChatList.Result
import com.ripenapps.adoreandroid.utils.CommonUtils

class AdapterChatsListing(val getSelectedChat: (Int, Result) -> Unit, var chatList: MutableList<Result>) :
    RecyclerView.Adapter<AdapterChatsListing.ViewHolder>() {
//    var list: MutableList<String> = mutableListOf()

    inner class ViewHolder(val binding: AdapterChatsListingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_chats_listing,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*if (position == chatList.size - 1) {    //set margin at the bottom of last chat item to align it above bottom navigation bar
            val layoutParams =
                (holder.binding.mainLayout?.layoutParams as? ViewGroup.MarginLayoutParams)
            layoutParams?.setMargins(
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._18sdp),
                0,
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._18sdp),
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp)
            )
            holder.binding.mainLayout?.layoutParams = layoutParams
        }*/
        holder.binding.onlineImage.isVisible = chatList[position].is_Online == "1"
        holder.binding.name.text = chatList[position].name
        if (chatList[position].unreadCount.toString()!="0"){
            holder.binding.messageCount.isVisible=true
            holder.binding.messageCount.text = chatList[position].unreadCount.toString()
        }else{
            holder.binding.messageCount.isVisible=false
        }
        if (chatList[position].messageType=="file"){
            if(chatList[position].lastMessage=="image"){
                holder.binding.description.text = holder.itemView.resources.getString(R.string.image)
                holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.last_message_image, 0, 0,0)
            }else if (chatList[position].lastMessage=="video"){
                holder.binding.description.text = holder.itemView.resources.getString(R.string.video)
                holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.last_message_video_two, 0, 0,0)
            }else  if (chatList.get(position).lastMessage=="audio"){
                holder.binding.description.text = holder.itemView.resources.getString(R.string.audio)
                holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_headphones_30, 0, 0,0)
            }else {
                holder.binding.description.text = holder.itemView.resources.getString(R.string.document)
                holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.document_icon, 0, 0,0)
            }
            holder.binding.description.compoundDrawablePadding = 10
        } else if (chatList[position].messageType=="videoCall"){
            holder.binding.description.text = holder.itemView.resources.getString(R.string.video_call)
            holder.binding.description.compoundDrawablePadding = 10
            holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.last_message_video, 0, 0,0)
        }else if (chatList[position].messageType=="audioCall"){
            holder.binding.description.text = holder.itemView.resources.getString(R.string.audio_call)
            holder.binding.description.compoundDrawablePadding = 10
            holder.binding.description.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_call_icon_black, 0, 0,0)
        } else{
            holder.binding.description.text = chatList[position].lastMessage
        }
        Glide.with(holder.itemView.context)
            .load(chatList[position].profile).placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.binding.profileImage)
//        Log.i("TAG", "convertTimestampToAndroidTime: "+CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "dd MMM yyyy hh:mm a"))
//        Log.i("TAG", "getFormattedDateYesterday: "+CommonUtils.getFormattedDateYesterday("dd MMM yyyy hh:mm a"))
//        Log.i("TAG", "getFormattedDateToday: "+CommonUtils.getFormattedDateToday("dd MMM yyyy hh:mm a"))
        if (CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "dd MMM yyyy")==CommonUtils.getFormattedDateToday("dd MMM yyyy")){
            holder.binding.time.text=CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "hh:mm a")
        } else if(CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "dd MMM yyyy")==CommonUtils.getFormattedDateYesterday("dd MMM yyyy")){
            holder.binding.time.text=holder.itemView.resources.getString(R.string.yesterday)
        } else{
            holder.binding.time.text=CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "dd MMM yyyy")
        }
//        holder.binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatList[position].updatedAt!!, "hh:mm a").toUpperCase()
        holder.binding.mainLayout.setOnClickListener {
            getSelectedChat(position, chatList[position])
        }
    }
    fun updateList(chatList: MutableList<Result>){
        this.chatList=chatList
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
//        list.removeAt(position)
//        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, list.size)
    }
}