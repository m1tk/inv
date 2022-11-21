package com.x.invid.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.x.invid.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings_view, Settings())
                    .commit()
    }
}