package com.x.invid

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VidHolder(item: View) : RecyclerView.ViewHolder(item) {
    var img_prev: ImageView
    var title: TextView
    var creator: TextView
    var views: TextView
    var date: TextView
    var length: TextView
    var id: String = ""

    init {
        img_prev = item.findViewById(R.id.vid_image)
        title    = item.findViewById(R.id.vid_title)
        creator  = item.findViewById(R.id.vid_creator)
        views    = item.findViewById(R.id.vid_views)
        date     = item.findViewById(R.id.vid_date)
        length   = item.findViewById(R.id.vid_length)
    }
}