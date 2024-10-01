package com.ripenapps.adoreandroid.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.ItemSpotBinding
import com.ripenapps.adoreandroid.models.response_models.cardlist.CardListUser
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE

class CardStackAdapter(
    private var context: Context,
    private var cardList: MutableList<CardListUser>,
    private var callbacks: CallBackInterfaces
) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    private var position = 0
    var listAdapterBinding: MutableList<ItemSpotBinding> = mutableListOf()
    private val screenWidth =
        context.resources.displayMetrics.widthPixels - context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._25sdp)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = ItemSpotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(inflater)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        this.position = holder.absoluteAdapterPosition
        listAdapterBinding.add(position, holder.binding)
        if (cardList[position]?.type.equals("ad")){
//            holder.binding.viewOverButtons.isClickable=false
            holder.binding.adCardview.isVisible=true
            holder.binding.title.text = cardList[position].title
            holder.binding.bannerUrl.text = cardList[position].bannerLink
            holder.binding.description.text = cardList[position].description
            holder.binding.adCardview.setOnClickListener {
//              disable the tap of overlapping card views and other views of this card
            }
            holder.binding.adText.setOnClickListener {

            }
            holder.binding.skipAdd.setOnClickListener {
                callbacks.callBack(4, position)
            }
            Glide.with(context)
                .load(cardList[position].bannerImage).placeholder(R.drawable.placeholder_image)
                .into(holder.binding.adImage)
        }else{
            holder.binding.adCardview.isVisible=false
            /*listAdapterBinding.map { binding:ItemSpotBinding->
                binding.dislikeButton.isClickable=true
                binding.likeButton.isClickable=true
                binding.rewindButton.isClickable=true
            }*/
            var imagesList = mutableListOf<String>()
            if (!cardList.isNullOrEmpty() && !cardList[position].media.isNullOrEmpty()) {
                cardList[position].media.forEachIndexed { index, mediaItem ->
                    if (!mediaItem.isNullOrEmpty()){
                        imagesList.add(mediaItem)
                    }
                }
            }
            var seekbarAdapter = AdapterCardSeekbar(0)
            var imageListPosition = 0
            if (!imagesList.isNullOrEmpty()) {
                if (imagesList.size > 0) {
                    Glide.with(context)
                        .load(imagesList[imageListPosition]).placeholder(R.drawable.placeholder_image)
                        .into(holder.binding.cardViewImage)
                    if (imagesList.size > 1) {
                        holder.binding.seekbarRecycler.isVisible = true
                        seekbarAdapter = imagesList?.let { AdapterCardSeekbar(it.size) }!!
                        val itemWidth =
                            (screenWidth - context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp) * imagesList.size) / imagesList.size
                        seekbarAdapter?.setItemWidth(itemWidth)
                        holder.binding.seekbarRecycler.adapter = seekbarAdapter
                    } else{
                        holder.binding.seekbarRecycler.isVisible = false
                    }
                }
            } else {
                Glide.with(holder.itemView.context)
                    .load(cardList[position].profileUrl)
                    .placeholder(R.drawable.placeholder_image)   //set a correct placeholder in every glide when available
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.binding.cardViewImage)
                holder.binding.seekbarRecycler.isVisible = false
            }
//            if(Preferences.getStringPreference(holder.itemView.context, SELECTED_LANGUAGE_CODE)!="ar"){
                holder.binding.age.text = " " + cardList[position].age
                holder.binding.name.text = getFirstWord(cardList[position].name)
//            }else{
//                holder.binding.age.text = " " + cardList[position].age
//                holder.binding.name.text = getFirstWord(cardList[position].name)
//            }
            if (!cardList?.get(position)?.city.isNullOrEmpty()) {
                holder.binding.location.text = cardList?.get(position)?.city
                if (cardList?.get(position)?.distance!=null){
                    holder.binding.location.text = cardList?.get(position)?.city+", "+getCharactersBeforeDot(cardList?.get(position)?.distance.toString())+" km"
                }
            }
            holder.binding.selectLeftImage.setOnClickListener {
                if (imageListPosition > 0) {
                    imageListPosition--
                    seekbarAdapter?.updatePosition(imageListPosition)
                    Glide.with(context)
                        .load(imagesList[imageListPosition]).placeholder(R.drawable.placeholder_image)
                        .into(holder.binding.cardViewImage)
                }
            }
            holder.binding.selectRightImage.setOnClickListener {
                if (imageListPosition < imagesList.size - 1) {
                    imageListPosition++
                    seekbarAdapter?.updatePosition(imageListPosition)
                    Glide.with(context)
                        .load(imagesList[imageListPosition]).placeholder(R.drawable.placeholder_image)
                        .into(holder.binding.cardViewImage)
                }
            }
            holder.binding.middleLayout.setOnClickListener {
//            setButtonDisable(holder)
                callbacks.callBack(2, position)
            }
            holder.binding.dislikeButton.setOnClickListener {
//            setButtonDisable(holder)
                callbacks.callBack(0, position)
            }
            holder.binding.likeButton.setOnClickListener {
//            setButtonDisable(holder)
                callbacks.callBack(1, position)
            }

            holder.binding.rewindButton.setOnClickListener {
                callbacks.callBack(3, position)
            }
        }
    }

    fun hideButtons() {
        listAdapterBinding.forEachIndexed { index, itemSpotBinding ->
            listAdapterBinding[index].idButtonContainer.isVisible = false
        }
    }

    fun showButtons() {
        listAdapterBinding.forEachIndexed { index, itemSpotBinding ->
            listAdapterBinding[index].idButtonContainer.isVisible = true
        }
    }
    /*fun showGradient(){
        listAdapterBinding.get(position).gradientImage.isVisible=true
    }
    fun hideGradient(){
        listAdapterBinding.get(position).gradientImage.isVisible=false
    }*/
    override fun getItemCount(): Int {
        return cardList.size
    }

    /*private fun setButtonDisable(holder: ViewHolder) {
        holder.binding.likeButton.isEnabled = false
        holder.binding.dislikeButton.isEnabled = false
        holder.binding.selectLeftImage.isEnabled = false
        holder.binding.selectRightImage.isEnabled = false
        setHandler(holder.binding.likeButton,holder.binding.dislikeButton,holder.binding.selectLeftImage,holder.binding.selectRightImage)
    }

    private fun setHandler(
        likeButton: ImageView,
        dislikeButton: ImageView,
        selectLeftImage: ConstraintLayout,
        selectRightImage: ConstraintLayout,
    ) {
        Handler().postDelayed({
            likeButton.isEnabled = true
            dislikeButton.isEnabled = true
            selectLeftImage.isEnabled = true
            selectRightImage.isEnabled = true
        }, 2000)
    }*/
    fun updateCardList(list: MutableList<CardListUser>) {
//        Handler().postDelayed({
//
//        }, 500)
        cardList = list
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
    class ViewHolder(val binding: ItemSpotBinding) : RecyclerView.ViewHolder(binding.root) {}

    interface CallBackInterfaces {
        fun callBack(id: Int, position: Int)
    }

}