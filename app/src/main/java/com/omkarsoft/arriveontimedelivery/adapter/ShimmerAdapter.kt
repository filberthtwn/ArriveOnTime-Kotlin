package com.omkarsoft.arriveontimedelivery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class ShimmerAdapter(
    val fragment: Fragment,
    val layout: Int,
    private val totalShimmer: Int
): RecyclerView.Adapter<ShimmerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int = totalShimmer

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}