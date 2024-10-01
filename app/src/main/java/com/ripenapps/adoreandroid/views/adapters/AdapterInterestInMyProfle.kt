package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSelectInterestsBinding
import com.ripenapps.adoreandroid.models.response_models.getInterestList.InterestData

class AdapterInterestInMyProfile(var selectedItemList:MutableList<InterestData?>?,var completeInterestList:MutableList<InterestData?>?, val flowCheck:String, val selector:InterestSelector): RecyclerView.Adapter<AdapterInterestInMyProfile.ViewHolder>() {
    var firstTimeUserSelectionChk=true
    inner class ViewHolder(val binding:AdapterSelectInterestsBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_select_interests, parent, false))
    }

    override fun getItemCount(): Int {
        Log.i("TAG", "getItemCount: "+selectedItemList+completeInterestList)
        return if (flowCheck=="fragment") selectedItemList?.size!!+1 else completeInterestList?.size!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (flowCheck=="fragment"){
            if (holder.absoluteAdapterPosition== selectedItemList?.size){//for change button
                holder.binding.model = InterestData("", "Change  ", true, "","")
                holder.binding.imageView.isVisible=false
            } else{//for showing interests in fragment
                holder.binding.imageView.isVisible=true
                holder.binding.model = selectedItemList?.get(position)
                Glide.with(holder.binding.root.context).load(selectedItemList?.get(position)?.iconeUrl)
                    .into(holder.binding.imageView)
            }
        }else{//for showing interests in bottomsheet
            if (firstTimeUserSelectionChk){
                for (item in selectedItemList!!){
                    if (item?._id== completeInterestList?.get(position)?._id){
                        completeInterestList?.get(position)?.isSelected =true
                    }
                }
            }
            if (position== (completeInterestList?.size!!-1)){
                firstTimeUserSelectionChk=false
            }
            holder.binding.model = completeInterestList?.get(position)
            Glide.with(holder.binding.root.context).load(completeInterestList?.get(position)?.iconeUrl)
                .into(holder.binding.imageView)
        }
        holder.binding.mainLayout.setOnClickListener {
            if (flowCheck=="fragment"){
                if (holder.absoluteAdapterPosition== selectedItemList?.size){
                    selector.openInterestBottomSheet()
                } else{

                }
            }else{
               if (completeInterestList?.get(position)?.isSelected == true) {
                   completeInterestList?.get(position)?.isSelected=false
                   notifyItemChanged(position)
               } else{
                   completeInterestList?.get(position)?.isSelected=true
                   notifyItemChanged(position)
               }
            }
        }
    }

    fun updateList(list:MutableList<InterestData?>?){
        selectedItemList = list
        notifyDataSetChanged()
    }
    fun getSelectedItemsList():MutableList<InterestData?>?{
        Log.i("TAG", "onClick: "+completeInterestList)
        var list:MutableList<InterestData?>?= mutableListOf()
        for (item in completeInterestList!!){
            if (item != null) {
                if (item.isSelected){
                    item.isSelected=false
                    list?.add(item)
                }
            }
        }
        return list
    }
    interface InterestSelector{
        fun openInterestBottomSheet()
        fun getSelectedInterestInBottomSheet()
    }
}