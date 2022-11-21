package com.x.invid.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.x.invid.Preferences
import com.x.invid.R


class Settings : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            when (it) {
                "video_quality" -> {
                    Preferences.video_quality = sharedPreferences?.getString("video_quality", null)!!.toInt()
                }
                "thumbnail_quality" -> {
                    Preferences.thumnail_res = sharedPreferences?.getString("thumbnail_quality", null)!!.toInt()
                }
                else -> {}
            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
    }
}
