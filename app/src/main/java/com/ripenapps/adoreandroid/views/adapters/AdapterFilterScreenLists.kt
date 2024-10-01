package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterFilterScreenListsBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData
import com.ripenapps.adoreandroid.preferences.UserPreference

class AdapterFilterScreenLists(
    val recyclerType: String,
    val list: MutableList<GenderData>,
    val setSelection: (Int, String, String) -> Unit,
    var selectedItem:String
) : RecyclerView.Adapter<AdapterFilterScreenLists.ViewHolder>() {
    constructor(
        recyclerType: String,
        list: MutableList<GenderData>
    ) : this(recyclerType, list, { i: Int, s: String,s1:String -> },"")

    var selectedPosition = -1
    var firstLoad=true

    inner class ViewHolder(val binding: AdapterFilterScreenListsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_filter_screen_lists,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (recyclerType == "interestedin") {
            holder.binding.name.setPadding(
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._25sdp),
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._7sdp),
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._25sdp),
                holder.itemView.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._7sdp)
            )
        }
        if ( recyclerType == "interestedin"||recyclerType=="sexualOrientation"||recyclerType=="lookingFor"||recyclerType=="complexion") {
            list[position].isSelected = selectedPosition == position
            if (firstLoad){
                if(list[position].name==selectedItem){
                    list[position].isSelected=true
                    selectedPosition=position
                    firstLoad=false
                }
            }
        }
       /* else if (recyclerType == "interestedin") {
            list[position].isSelected = selectedPosition == position
            if (firstLoad){
                if(list[position].name.equals(selectedItem, ignoreCase = true)){
                    list[position].isSelected=true
                    selectedPosition=position
                    firstLoad=false
                }
            }
        }*/
        else if (recyclerType=="sortby"){
            list[position].isSelected = selectedPosition == position
            if (firstLoad){
                if((list[position].name.toString()).equals(selectedItem, ignoreCase = true)){
                    list[position].isSelected=true
                    selectedPosition=position
                    firstLoad=false
                }
            }
        }
        else if (recyclerType=="interest"){
            if (firstLoad){
                Log.i("TAG", "onBindViewHolder inside: "+list[position].isSelected)
                if (UserPreference.filterCardListRequestKeys.whatElseYouLike.contains(list[position]._id)){
                    list[position].isSelected=true
                }
                if (position==list.size-1)
                    firstLoad=false
                Log.i("TAG", "firstLoad : "+firstLoad)

            }
        }
        holder.binding.model = list[position]
        holder.binding.name.setOnClickListener {
            if (recyclerType == "interestedin" || recyclerType == "sortby"||recyclerType=="sexualOrientation"||recyclerType=="lookingFor"||recyclerType=="complexion") {
                if (selectedPosition != position) {
                    list[position].isSelected = true
                    if (selectedPosition != -1) {
                        list[selectedPosition].isSelected = false
                        notifyItemChanged(selectedPosition)
                    }
                    notifyItemChanged(position)
                    selectedPosition = position
                    selectedItem= list[position].name.toString()
                    setSelection(position, selectedItem, recyclerType)
                } else {
                    if (recyclerType!="interestedin"){  // interestedin cann't be unselected
                        list[position].isSelected = false
                        notifyItemChanged(position)
                        selectedPosition = -1
                        selectedItem=""
                        setSelection(-1, "", recyclerType)
                    }
                }
            } else if (recyclerType == "interest") {
                if (list[position].isSelected == true) {
                    Log.i("TAG", "onBindViewHolder: "+"true")
                    list[position].isSelected = false
                    notifyItemChanged(position)
                } else {
                    Log.i("TAG", "onBindViewHolder: "+"false")
                    list[position].isSelected = true
                    notifyItemChanged(position)
                }
            }
        }
    }

    /*fun selectedWhatAreYouIntoList(): MutableList<GenderData> {
        var selectedList = mutableListOf<GenderData>()
        list.forEachIndexed { index, item ->
            if (item.isSelected == true) {
                selectedList.add(item)
            }
        }
        return selectedList
    }*/
    fun selectedWhatAreYouIntoList(): MutableList<String> {
        var selectedList = mutableListOf<String>()
        list.forEachIndexed { index, item ->
            if (item.isSelected == true) {
                selectedList.add(item?._id!!)
            }
        }
        return selectedList
    }
    fun removeSpaces(input: String): String {
        return input.replace(" ", "")
    }
}