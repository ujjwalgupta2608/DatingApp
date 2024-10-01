package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterChatOtherTextBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatUserTextBinding
import com.ripenapps.adoreandroid.models.response_models.helpList.ChatMessage
import com.ripenapps.adoreandroid.utils.CommonUtils

class AdapterHelp(var chatMessagesList: MutableList<ChatMessage>?, val myId: String?, val closeKeyboard:()->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_OTHER_TEXT = 0
    private val VIEW_TYPE_USER_TEXT = 1
    var itemType=0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_OTHER_TEXT -> OtherTextViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.adapter_chat_other_text, parent, false
                )
            )

            VIEW_TYPE_USER_TEXT -> UserTextViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.adapter_chat_user_text, parent, false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")

        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (chatMessagesList?.get(position)?.receiver==myId)
            VIEW_TYPE_OTHER_TEXT
        else
            VIEW_TYPE_USER_TEXT
    }
    inner class OtherTextViewHolder(val binding: AdapterChatOtherTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            binding.senderMessage.text = chatMessage.message
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a")
            binding.topLayout.setOnClickListener { closeKeyboard() }
        }
    }

    inner class UserTextViewHolder(val binding: AdapterChatUserTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }

            binding.senderMessage.text = chatMessage.message
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a")
            binding.topLayout.setOnClickListener { closeKeyboard() }

        }
    }
    override fun getItemCount(): Int {
        return chatMessagesList?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OtherTextViewHolder -> {
                holder.bind(chatMessagesList?.get(position)!!)
            }

            is UserTextViewHolder -> {
                holder.bind(chatMessagesList?.get(position)!!)
            }

        }
    }
    /*fun updateMessageList(chatMessagesList: MutableList<ChatMessage>) {
        this.chatMessagesList=chatMessagesList
        notifyDataSetChanged()
    }*/
}