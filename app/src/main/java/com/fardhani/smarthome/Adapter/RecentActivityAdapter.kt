package com.fardhani.smarthome.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fardhani.smarthome.Model.RecentActivityModel
import com.fardhani.smarthome.R
import kotlinx.android.synthetic.main.item_recent_activity.view.*

//Adapter for recent activity recyclerview
class RecentActivityAdapter(private val recentActivities: ArrayList<RecentActivityModel>) :
    RecyclerView.Adapter<RecentActivityAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_recent_activity,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = recentActivities.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) =
        holder.bind(recentActivities[position])

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(recentActivity: RecentActivityModel) {
            with(itemView) {
                txtActivity.text = recentActivity.activity
                txtTime.text = recentActivity.time
                txtUidName.text = recentActivity.uid_name
            }
        }
    }
}