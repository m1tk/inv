package com.x.invid.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.x.invid.R

class Settings : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}
