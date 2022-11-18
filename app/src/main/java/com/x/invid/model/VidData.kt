package com.x.invid.model

class VidData(val img_prev: String, val title: String, val creator: String,
              views_num: Long, val date: String, val len_secs: Long, val id: String) {
    val views: String = k_m_b_generator(views_num)

    fun k_m_b_generator(num: Long) : String {
        return if (num in 1000..99998) {
            "${String.format("%.1f", (num.toDouble() / 1000.0))} K";
        } else if (num in 100000..999998) {
            "${String.format("%.0f", (num.toDouble() / 1000.0))} K";
        } else if (num in 1000000..999999998) {
            "${String.format("%.1f", (num.toDouble() / 1000000.0))} M";
        } else if (num > 999999999) {
            "${String.format("%.1f", (num.toDouble() / 1000000000.0))} B";
        } else {
            num.toString();
        }
    }
}