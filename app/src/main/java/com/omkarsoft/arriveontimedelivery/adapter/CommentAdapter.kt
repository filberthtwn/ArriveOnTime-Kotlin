package com.omkarsoft.arriveontimedelivery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omkarsoft.arriveontimedelivery.R
import com.omkarsoft.arriveontimedelivery.data.model.Notification
import com.omkarsoft.arriveontimedelivery.databinding.ItemCommentsBinding

class CommentAdapter(
    var data: List<Notification>
): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    fun updateData(data: List<Notification>){
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemCommentsBinding>(layoutInflater, R.layout.item_comments, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    inner class ViewHolder(var binding: ItemCommentsBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(notification: Notification){
            binding.notification = notification
            binding.executePendingBindings()
        }
    }
}