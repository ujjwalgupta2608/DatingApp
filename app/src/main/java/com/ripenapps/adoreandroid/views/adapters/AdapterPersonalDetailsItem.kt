package com.ripenapps.adoreandroid.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterPersonalDetailsItemsBinding
import com.ripenapps.adoreandroid.models.response_models.getquestions.GenderData

class AdapterPersonalDetailsItem(
    val list: List<GenderData>?,
    val getSelectedItem: (Int, String) -> Unit,
    var selectedItem: String,
    var listCheck: String,
    val selectionType:String="single",
    private var selectedItemList:MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<AdapterPersonalDetailsItem.ViewHolder>() {
    var selectedPosition = -1

    inner class ViewHolder(val binding: AdapterPersonalDetailsItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_personal_details_items,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectionType=="multiple"){
            if (selectedItemList.contains(list?.get(position)?.name)){
                list?.get(position)?.isSelected=true
            }
        }else{
            if (selectedItem != "") {
                if (selectedItem.equals(list?.get(position)?.name, ignoreCase = true)) {
                    list?.get(position)?.isSelected  = true
                    selectedItem = ""
                    selectedPosition = position
                } else {
                    list?.get(position)?.isSelected = false
                }
            }
        }
        holder.binding.model = list?.get(position)
        holder.binding.name.setOnClickListener {
            if (selectionType=="multiple"){
                if (list?.get(position)?.isSelected==true){
                    list?.get(position)?.isSelected = false
                    selectedItemList.remove(list?.get(position)?.name)
                    notifyItemChanged(position)
                } else{
                    list?.get(position)?.isSelected = true
                    list?.get(position)?.name?.let { it1 -> selectedItemList.add(it1) }
                    notifyItemChanged(position)
                }
            } else{
                selectedItem = ""
                if (position != selectedPosition) {
                    list?.get(position)?.isSelected = true
                    notifyItemChanged(position)
                    if (selectedPosition != -1){
                        list?.get(selectedPosition)?.isSelected = false
                        notifyItemChanged(selectedPosition)
                    }
                    selectedPosition = position
                    list?.get(position)?.name?.let { it1 -> getSelectedItem(position, it1) }
                } else if (listCheck=="drink"||listCheck=="smoke"||listCheck=="profession"||listCheck=="pets"||listCheck=="communication"||listCheck=="education"||listCheck=="zodiac"||listCheck == "sexualOrientation"||listCheck == "complexion"||listCheck == "language"||listCheck == "relationshipGoal"||listCheck == "interestedIn"){
                    list?.get(position)?.isSelected = false
                    notifyItemChanged(position)
                    getSelectedItem(position, "")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.size!!
    }
    fun getSelectedItemList():MutableList<String>{
        return selectedItemList
    }

}