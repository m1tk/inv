package com.x.invid

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class Preferences {
    companion object {
        var thumnail_res: Int  = 0
        var video_quality: Int = 0

    }

    fun init(ctx: Context) {
        var prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(ctx)


        var editor = prefs.edit()
        // If thumbnail quality not set default is 1280
        var tres = prefs.getString("thumbnail_quality", null)
        tres?:run {
            tres = "640"
            editor.putString("thumbnail_quality", tres)
        }
        thumnail_res  = tres!!.toInt()
        // Same for video
        var vres = prefs.getString("video_quality", null)
        vres?:run {
            vres = "720"
            editor.putString("video_quality", vres)
        }
        video_quality = vres!!.toInt()

        editor.commit()
    }
}