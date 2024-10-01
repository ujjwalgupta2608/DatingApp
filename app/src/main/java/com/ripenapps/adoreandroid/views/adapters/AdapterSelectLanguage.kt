package com.ripenapps.adoreandroid.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.AdapterSelectLanguageBinding
import com.ripenapps.adoreandroid.models.static_models.SelectLanguageModel
import com.ripenapps.adoreandroid.preferences.Preferences
import com.ripenapps.adoreandroid.preferences.SELECTED_LANGUAGE_CODE

class AdapterSelectLanguage(var list: MutableList<SelectLanguageModel>, var getSelectedLanguage: (Int) -> Unit, var selectedPos:Int): RecyclerView.Adapter<AdapterSelectLanguage.ViewHolder>() {

    inner class ViewHolder(itemView:AdapterSelectLanguageBinding):RecyclerView.ViewHolder(itemView.root){
        var binding:AdapterSelectLanguageBinding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.adapter_select_language,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.model = list[position]
        holder.binding.mainLayout.setOnClickListener {
            Log.i("TAG", "onBindViewHolder: $selectedPos")
            if (selectedPos!=position){
                list[position].isSelected=true
                list[selectedPos].isSelected=false
                notifyItemChanged(position)
                notifyItemChanged(selectedPos)
                selectedPos=position
                getSelectedLanguage(selectedPos)
            }
        }

    }
    fun updateList(list: MutableList<SelectLanguageModel>){
        this.list = list
        notifyDataSetChanged()
    }

}