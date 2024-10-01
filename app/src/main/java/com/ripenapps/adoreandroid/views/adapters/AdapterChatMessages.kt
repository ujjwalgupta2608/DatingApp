package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterChatOtherAudioBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatOtherMediaBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatOtherTextBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatUserAudioBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatUserMediaBinding
import com.ripenapps.adoreandroid.databinding.AdapterChatUserTextBinding
import com.ripenapps.adoreandroid.models.response_models.carddetails.Media
import com.ripenapps.adoreandroid.models.response_models.receiveMessageResponse.ReceivedMessageResponse
import com.ripenapps.adoreandroid.utils.CommonUtils


class AdapterChatMessages(
    var messagesList: MutableList<ReceivedMessageResponse>,
    val myId: String?,
    val getSelectedItem: (String, String) -> Unit,
    val closeKeyboard:()->Unit,
    val audioPlayInterface: AudioPlayInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_OTHER_TEXT = 0
    private val VIEW_TYPE_USER_TEXT = 1
    private val VIEW_TYPE_MEDIA = 2
    private val VIEW_TYPE_OTHER_MEDIA = 3
    private val VIEW_TYPE_USER_AUDIO = 4
    private val VIEW_TYPE_OTHER_AUDIO = 5
    private var previousActivePos=-1

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

            VIEW_TYPE_MEDIA -> UserMediaViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.adapter_chat_user_media, parent, false
                )
            )

            VIEW_TYPE_OTHER_MEDIA -> {
                OtherMediaViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.adapter_chat_other_media, parent, false
                    )
                )
            }
            VIEW_TYPE_USER_AUDIO -> {
                UserAudioViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.adapter_chat_other_audio, parent, false
                    )
                )
            }

            VIEW_TYPE_OTHER_AUDIO -> {
                OtherAudioViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.adapter_chat_other_audio, parent, false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    inner class OtherMediaViewHolder(val binding: AdapterChatOtherMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(chatMessage: ReceivedMessageResponse) {
                if (chatMessage.dateToShow!=""){
                    binding.date.isVisible=true
                    binding.date.text = chatMessage.dateToShow
                }else{
                    binding.date.isVisible=false
                }
                if (!chatMessage.media.isNullOrEmpty() && chatMessage.media.size > 3) {
                    /*Glide.with(itemView.context).load(chatMessage.media[0].mediaUrl)
                        .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                        .into(binding.image1)
                    Glide.with(itemView.context).load(chatMessage.mediaList[1].mediaUrl)
                        .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                        .into(binding.image2)
                    Glide.with(itemView.context).load(chatMessage.mediaList[2].mediaUrl)
                        .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                        .into(binding.image3)
                    Glide.with(itemView.context).load(chatMessage.mediaList[3].mediaUrl)
                        .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                        .into(binding.image4)
                    if (chatMessage.media.size > 4) {
                        binding.additionalMediaText.isVisible = true
                        binding.additionalMediaText.setBackgroundResource(R.color.grey_boulder_30per)
                        binding.additionalMediaText.text =
                            "+" + (chatMessage.media.size - 4).toString()
                    } else {
                        binding.additionalMediaText.isVisible = false
                    }*/
                } else {
                    binding.image2.isVisible = false
                    binding.image3.isVisible = false
                    binding.image4.isVisible = false
                    if (chatMessage.media!=null){
                        if (chatMessage.media.size>0){
//                            binding.innerLayout.setBackgroundResource(R.color.grey_boulder_30per)
                            val layoutParams = binding.image1.layoutParams
                            if (chatMessage.media[0]?.type=="image"){
                                binding.videoIcon.isVisible=false
                                layoutParams.width = 0
                                layoutParams.height = 0
                                Glide.with(itemView.context).load(chatMessage.media[0]?.image)
                                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                                    .into(binding.image1)
                            }else if (chatMessage.media[0]?.type=="video"){
                                binding.videoIcon.isVisible=true
                                layoutParams.width = 0
                                layoutParams.height = 0
                                Glide.with(itemView.context).load(chatMessage.media[0]?.thumbnail)
                                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                                    .into(binding.image1)
                            }else {
                                layoutParams.width = itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)
                                layoutParams.height = itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)
                                when (chatMessage.media[0]?.type){
                                    "pdf"-> binding.image1.setImageResource(R.drawable.pdf_file)
                                    "text"-> binding.image1.setImageResource(R.drawable.text_file)
                                    "excel"-> binding.image1.setImageResource(R.drawable.excel_file)
                                    "word"-> binding.image1.setImageResource(R.drawable.word_file)

                                }
                            }
                            binding.image1.layoutParams = layoutParams

                        }

                    }
                    binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
                    binding.cardView.setOnClickListener {
                        if (chatMessage.media!=null){
                            if (chatMessage.media.size>0){
                                if (chatMessage.media[0]?.type=="image"){
                                    getSelectedItem("image", chatMessage.media[0]?.image!!)
                                }else if (chatMessage.media[0]?.type=="video"){
                                    getSelectedItem("video", chatMessage.media[0]?.video!!)
                                } else{
                                    getSelectedItem(chatMessage.media[0]?.type!!, chatMessage.media[0]?.document!!)
                                }
                            }

                        }
                    }
                    binding.topLayout.setOnClickListener { closeKeyboard() }
                }
            }
    }

    inner class UserMediaViewHolder(val binding: AdapterChatUserMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ReceivedMessageResponse) {
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            if (!chatMessage.media.isNullOrEmpty() && chatMessage.media.size > 3) {
                /*Glide.with(itemView.context).load(chatMessage.media[0].mediaUrl)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    .into(binding.image1)
                Glide.with(itemView.context).load(chatMessage.mediaList[1].mediaUrl)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    .into(binding.image2)
                Glide.with(itemView.context).load(chatMessage.mediaList[2].mediaUrl)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    .into(binding.image3)
                Glide.with(itemView.context).load(chatMessage.mediaList[3].mediaUrl)
                    .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                    .into(binding.image4)
                if (chatMessage.media.size > 4) {
                    binding.additionalMediaText.isVisible = true
                    binding.additionalMediaText.setBackgroundResource(R.color.grey_boulder_30per)
                    binding.additionalMediaText.text =
                        "+" + (chatMessage.media.size - 4).toString()
                } else {
                    binding.additionalMediaText.isVisible = false
                }*/
            } else {
                binding.image2.isVisible = false
                binding.image3.isVisible = false
                binding.image4.isVisible = false
                if (chatMessage.media!=null){
                    if (chatMessage.media.size>0){
//                        binding.innerLayout.setBackgroundResource(R.color.grey_boulder_30per)
                        val layoutParams = binding.image1.layoutParams
                        if (chatMessage.media[0]?.type=="image"){
                            binding.videoIcon.isVisible=false
                            layoutParams.width = 0
                            layoutParams.height = 0
                            Glide.with(itemView.context).load(chatMessage.media[0]?.image)
                                .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                                .into(binding.image1)
                        }else if (chatMessage.media[0]?.type=="video"){
                            binding.videoIcon.isVisible=true
                            layoutParams.width = 0
                            layoutParams.height = 0
                            Glide.with(itemView.context).load(chatMessage.media[0]?.thumbnail)
                                .placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image)
                                .into(binding.image1)
                        }else {
                            layoutParams.width = itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)
                            layoutParams.height = itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)
                            when (chatMessage.media[0]?.type){
                                "pdf"-> binding.image1.setImageResource(R.drawable.pdf_file)
                                "text"-> binding.image1.setImageResource(R.drawable.text_file)
                                "excel"-> binding.image1.setImageResource(R.drawable.excel_file)
                                "word"-> binding.image1.setImageResource(R.drawable.word_file)

                            }
                        }
                        binding.image1.layoutParams = layoutParams

                    }

                }
                binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
                binding.cardView.setOnClickListener {
                    if (chatMessage.media!=null){
                        if (chatMessage.media.size>0){
                            if (chatMessage.media[0]?.type=="image"){
                                getSelectedItem("image", chatMessage.media[0]?.image!!)
                            }else if (chatMessage.media[0]?.type=="video"){
                                getSelectedItem("video", chatMessage.media[0]?.video!!)
                            } else{
                                getSelectedItem(chatMessage.media[0]?.type!!, chatMessage.media[0]?.document!!)
                            }

                        }

                    }
                }
                binding.topLayout.setOnClickListener { closeKeyboard() }
            }
        }
    }

    inner class OtherTextViewHolder(val binding: AdapterChatOtherTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ReceivedMessageResponse) {
            val gestureDetector =
                GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        audioPlayInterface.copyText(chatMessage.message.toString())
                    }
                    override fun onSingleTapUp(p0: MotionEvent): Boolean {
//                        if (chatMessage.messageType=="text"&&chatMessage.callStatus.isNullOrEmpty()) {
                            closeKeyboard()
//                        }
                        return true
                    }
                })
            binding.root.setOnTouchListener { _, event ->
                gestureDetector?.onTouchEvent(event)
                true
            }
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            binding.replyText.isVisible = chatMessage.isStory == true
            binding.senderMessage.text = chatMessage.message
            binding.senderMessage.setPadding(20, 20, 20, 0)
            if (chatMessage.messageType=="videoCall"||chatMessage.messageType=="audioCall"){
                if (chatMessage.messageType=="videoCall") {
                    binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_call_icon_two, 0, 0,0)
                }else{
                    binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_call_icon_two, 0, 0,0)
                }
                binding.senderMessage.compoundDrawablePadding = 20
                if (chatMessage.callStatus=="missed"){
                    binding.senderMessage.text = itemView.resources.getString(R.string.missed_call)
                } else if (chatMessage.callStatus=="ended"){
                    binding.senderMessage.text = "${itemView.resources.getString(R.string.call_duration)} ${CommonUtils.formatSecondsToMinutesAndSeconds(chatMessage.duration)}"
                } else if(chatMessage.callStatus=="rejected"){
                    binding.senderMessage.text = itemView.resources.getString(R.string.call_rejected)
                }else{
                    binding.senderMessage.text = itemView.resources.getString(R.string.missed_call)
                }
            }else if (chatMessage.messageType=="text"&&chatMessage.callStatus=="queued"){
//                binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_call_icon, 0, 0,0)
                binding.senderMessage.text = "  ${itemView.resources.getString(R.string.missed_call)}"
            }
            else{
                binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
            }
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
//            binding.topLayout.setOnClickListener { closeKeyboard() }
        }
    }

    inner class UserTextViewHolder(val binding: AdapterChatUserTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ReceivedMessageResponse) {
            val gestureDetector =
                GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        if (chatMessage.messageType=="text"&&chatMessage.callStatus.isNullOrEmpty())
                            audioPlayInterface.copyText(chatMessage.message.toString())
                    }
                    override fun onSingleTapUp(p0: MotionEvent): Boolean {
//                        if (chatMessage.messageType=="text"&&chatMessage.callStatus.isNullOrEmpty()) {
                        closeKeyboard()
//                        }
                        return true
                    }
                })
            binding.root.setOnTouchListener { _, event ->
                gestureDetector?.onTouchEvent(event)
                true
            }
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            binding.senderMessage.text = chatMessage.message
            binding.senderMessage.setPadding(20, 20, 20, 0)
            if (chatMessage.messageType=="videoCall"||chatMessage.messageType=="audioCall"){
                if (chatMessage.messageType=="videoCall") {
                    binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_call_icon, 0, 0,0)
                }else{
                    binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_call_icon, 0, 0,0)
                }
                binding.senderMessage.compoundDrawablePadding = 20
                if (chatMessage.callStatus=="missed"){
                    binding.senderMessage.text = itemView.resources.getString(R.string.missed_call)
                } else if (chatMessage.callStatus=="ended"){
                    binding.senderMessage.text = "${itemView.resources.getString(R.string.call_duration)} ${CommonUtils.formatSecondsToMinutesAndSeconds(chatMessage.duration)}"
                } else if(chatMessage.callStatus=="rejected"){
                    binding.senderMessage.text = itemView.resources.getString(R.string.call_rejected)
                }else{
                    binding.senderMessage.text = itemView.resources.getString(R.string.missed_call)
                }
            }else if (chatMessage.messageType=="text"&&chatMessage.callStatus=="queued"){
//                binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video_call_icon, 0, 0,0)
                binding.senderMessage.text = "  ${itemView.resources.getString(R.string.missed_call)}"
            }
            else{
                binding.senderMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0,0)
            }
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
//            binding.topLayout.setOnClickListener { closeKeyboard() }
        }
    }

    inner class UserAudioViewHolder(val binding: AdapterChatOtherAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ReceivedMessageResponse) {
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            binding.innerLayout.background = itemView.resources.getDrawable(R.drawable.background_receiver_text)
            binding.topLayout.gravity = Gravity.END
            binding.pause.setBackgroundResource(R.drawable.pause_audio)
            binding.playIcon.setBackgroundResource(R.drawable.play_audio)
            binding.playerTime.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.time.setTextColor(itemView.resources.getColor(R.color.white_whisper))
            binding.idSeekBar.thumbTintList = ContextCompat.getColorStateList(binding.root.context, R.color.white_whisper)
            binding.idSeekBar.progressBackgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.white_whisper)
            binding.idSeekBar.progressTintList = ContextCompat.getColorStateList(binding.root.context, R.color.white_whisper)
            binding.idSeekBar.secondaryProgressTintList = ContextCompat.getColorStateList(binding.root.context, R.color.white_whisper)
            binding.playIcon.setOnClickListener {
                audioPlayInterface.onPlayOtherAudio(binding, chatMessage.media[0]?.document!!, position, previousActivePos)
                previousActivePos=position
            }
            binding.pause.setOnClickListener {
                audioPlayInterface.onPauseOtherAudio(binding, chatMessage.media[0]?.document!!, position, previousActivePos)
            }
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
            binding.topLayout.setOnClickListener { closeKeyboard() }
        }
    }
    inner class OtherAudioViewHolder(val binding: AdapterChatOtherAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ReceivedMessageResponse) {
            if (chatMessage.dateToShow!=""){
                binding.date.isVisible=true
                binding.date.text = chatMessage.dateToShow
            }else{
                binding.date.isVisible=false
            }
            binding.playIcon.setOnClickListener {
                audioPlayInterface.onPlayOtherAudio(binding, chatMessage.media[0]?.document!!, position, previousActivePos)
                previousActivePos=position
            }
            binding.pause.setOnClickListener {
                audioPlayInterface.onPauseOtherAudio(binding, chatMessage.media[0]?.document!!, position, previousActivePos)
            }
            binding.time.text = CommonUtils.convertTimestampToAndroidTime(chatMessage.createdAt!!, "hh:mm a").toUpperCase()
            binding.topLayout.setOnClickListener { closeKeyboard() }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OtherTextViewHolder -> {
                holder.bind(messagesList[position])
            }
            is UserTextViewHolder -> {
                holder.bind(messagesList[position])
            }
            is UserMediaViewHolder -> {
                holder.bind(messagesList[position])
            }
            is OtherMediaViewHolder -> {
                holder.bind(messagesList[position])
            }
            is UserAudioViewHolder -> {
                holder.bind(messagesList[position])
            }
            is OtherAudioViewHolder -> {
                holder.bind(messagesList[position])
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if ((messagesList[position].messageType=="text"||messagesList[position].messageType=="videoCall"||messagesList[position].messageType=="audioCall")&&!messagesList[position].receiverId.equals(myId))
            VIEW_TYPE_USER_TEXT
        else if ((messagesList[position].messageType=="text"||messagesList[position].messageType=="videoCall"||messagesList[position].messageType=="audioCall")&&messagesList[position].receiverId.equals(myId))
            VIEW_TYPE_OTHER_TEXT
        else if (messagesList[position].messageType=="file"&&!messagesList[position].receiverId.equals(myId)&&(messagesList[position].media[0]?.type=="mp3"||messagesList[position].media[0]?.type=="audio"))
            VIEW_TYPE_USER_AUDIO
        else if (messagesList[position].messageType=="file"&&messagesList[position].receiverId.equals(myId)&&(messagesList[position].media[0]?.type=="mp3"||messagesList[position].media[0]?.type=="audio"))
            VIEW_TYPE_OTHER_AUDIO
        else if (messagesList[position].messageType=="file"&&!messagesList[position].receiverId.equals(myId))
            VIEW_TYPE_MEDIA
        else if (messagesList[position].messageType=="file"&&messagesList[position].receiverId.equals(myId))
            VIEW_TYPE_OTHER_MEDIA
        else
            -1
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
    interface AudioPlayInterface {
        fun copyText(text:String)
        fun onPlayOtherAudio(binding: AdapterChatOtherAudioBinding, s: String, position: Int, prevActivePos:Int)
        fun onPauseOtherAudio(binding: AdapterChatOtherAudioBinding, s: String, position: Int, prevActivePos:Int)
    }
    fun updateList(list: MutableList<ReceivedMessageResponse>){
        messagesList = list
        notifyDataSetChanged()
    }

    fun updateMessage(chatMessage: ReceivedMessageResponse) {
        messagesList.add(chatMessage)
        notifyItemChanged(messagesList.size - 1)
    }

}