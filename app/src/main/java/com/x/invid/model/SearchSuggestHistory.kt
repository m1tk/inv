package com.x.invid.model

import android.app.SearchManager
import android.content.Intent
import android.content.SearchRecentSuggestionsProvider
import android.provider.SearchRecentSuggestions

class SearchSuggestHistory : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.x.invid_search_history"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
    
}