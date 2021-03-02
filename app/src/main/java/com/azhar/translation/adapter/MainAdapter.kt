package com.azhar.translation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azhar.translation.R
import com.azhar.translation.model.ModelMain
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.list_item_translate.view.*

/**
 * Created by Azhar Rivaldi on 25-02-2021
 */

class MainAdapter(private val items: List<ModelMain>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_translate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTranslation.text = data.strTranslation
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position)
    }

    internal class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var timelineView: TimelineView
        var tvTranslation: TextView

        init {
            timelineView = itemView.timelineView
            timelineView.initLine(viewType)
            tvTranslation = itemView.tvTranslation
        }
    }

}