package com.x.invid

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VidRecycler(private val list: List<VidData>, private val ctx: Context,
                  private val callback: (id: String) -> Unit) : RecyclerView.Adapter<VidHolder>() {
    private fun seconds_to_time(secs: Long) : String {
        return if (secs < 60) {
            secs.toString()
        } else {
            DateUtils.formatElapsedTime(secs)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VidHolder {
        val ctx = parent.context;
        var inf = LayoutInflater.from(ctx)

        val view = inf.inflate(R.layout.vid_display, parent, false)

        return VidHolder(view)
    }

    override fun onBindViewHolder(holder: VidHolder, position: Int) {
        val ind             = holder.adapterPosition
        holder.id           = list[position].id
        Glide.with(holder.img_prev.context)
            .load(list[position].img_prev)
            .placeholder(R.drawable.video_thumbnail)
            .error(R.drawable.video_thumbnail)
            .into(holder.img_prev)
        holder.img_prev.setOnClickListener { callback.invoke(holder.id) }
        holder.title.text   = list[position].title
        holder.title.setOnClickListener { callback.invoke(holder.id) }
        holder.creator.text = list[position].creator
        holder.views.text   = list[position].views
        holder.date.text    = list[position].date
        if (list[position].len_secs != 0L) {
            holder.length.text = seconds_to_time(list[position].len_secs)
        } else {
            holder.length.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}