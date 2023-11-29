package com.example.rurutektrack

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION_CODES.N
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import okhttp3.internal.http2.Http2Reader
import java.util.ArrayList

class TimelineAdapter(private var timelineDataList: List<TimelineData>)
    : RecyclerView.Adapter<TimelineAdapter.RecycleViewHolder>() {

    class RecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val endTimeTextView: TextView = itemView.findViewById(R.id.endTimeTextView)
        val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        val startKmTextView: TextView = itemView.findViewById(R.id.startKmTextView)
        val endKmTextView: TextView = itemView.findViewById(R.id.endKmTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard, parent, false)
        return RecycleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        val event = timelineDataList[position]
        holder.titleTextView.text = event.model
        holder.endTimeTextView.text = event.end_time
        holder.placeTextView.text = event.place
        holder.startKmTextView.text = event.startkm
        holder.endKmTextView.text = event.endkm
        if (event.place == null||event.place.isEmpty()) {
            holder.itemView.findViewById<LinearLayout>(R.id.recycle).setOnClickListener {
                onItemClick?.invoke(event)
                val intent = Intent(holder.itemView.context, EndActivity::class.java)
                intent.putExtra("user", event.user)
                intent.putExtra("model", event.model)
                intent.putExtra("startkm", event.startkm)
                holder.itemView.context.startActivity(intent)

            }
        }


    }

    override fun getItemCount(): Int {
        Log.d("TimelineAdapter", "ItemCount: ${timelineDataList.size}")
        return timelineDataList.size
    }

    companion object {
        var onItemClick: ((TimelineData) -> Unit)? = null

    }
    fun setData(newTimelineDataList: List<TimelineData>) {
        timelineDataList = newTimelineDataList
        notifyDataSetChanged()
    }


}