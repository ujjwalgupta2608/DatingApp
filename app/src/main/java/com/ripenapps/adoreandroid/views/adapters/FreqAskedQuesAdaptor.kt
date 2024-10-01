package com.ripenapps.adoreandroid.views.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ripenapps.adoreandroid.R
import com.ripenapps.adoreandroid.databinding.FreqAskedQuestionRecyclerBinding
import com.ripenapps.adoreandroid.models.response_models.faqResponse.Faq

class FreqAskedQuesAdaptor(faqList: MutableList<Faq>?) : RecyclerView.Adapter<FreqAskedQuesAdaptor.ViewHolder>() {
    var selectedPosition = -1
    var faqList=faqList

    inner class ViewHolder(itemView: FreqAskedQuestionRecyclerBinding) :RecyclerView.ViewHolder(itemView.root){
        var binding:FreqAskedQuestionRecyclerBinding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.freq_asked_question_recycler, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.question.text = faqList?.get(position)?.title
        holder.binding.answer.text = Html.fromHtml(faqList?.get(position)?.description)

        if (position != selectedPosition) {
            holder.binding.answer.visibility = View.GONE
            holder.binding.imageViewArrow.rotation = 0F
            holder.binding.line.visibility = View.GONE
        }
        holder.binding.question.setOnClickListener { v ->
            if (holder.binding.answer.visibility == View.GONE) {
                holder.binding.line.visibility = View.VISIBLE
                holder.binding.answer.visibility = View.VISIBLE
                holder.binding.imageViewArrow.rotation = 180F
                selectedPosition = position
                notifyDataSetChanged()
            } else if (holder.binding.answer.visibility == View.VISIBLE) {
                holder.binding.line.visibility = View.GONE
                holder.binding.answer.visibility = View.GONE
                holder.binding.imageViewArrow.rotation = 0F
                selectedPosition = position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return faqList?.size!!
    }

    fun updateList(list:MutableList<Faq>){
        this.faqList=list
        notifyDataSetChanged()
    }
}