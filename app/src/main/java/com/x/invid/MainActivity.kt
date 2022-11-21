package com.x.invid

import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_player.view.*
import retrofit2.Response
import com.x.invid.api.ClientBuilder
import com.x.invid.api.Video
import com.x.invid.adapter.VidHolder
import com.x.invid.adapter.VidRecycler
import com.x.invid.model.SearchSuggestHistory
import com.x.invid.model.VidData
import com.x.invid.ui.RecycleViewItemDecoration
import com.x.invid.ui.SettingsActivity
import com.x.invid.ui.VideoPlayer

class MainActivity : AppCompatActivity() {
    companion object {
        var client = ClientBuilder()
            .get_client()

        // Remember objects of views
        var cur_popular: Pair<ArrayList<VidData>, API_ERROR>?  = null
        var cur_trending: Pair<ArrayList<VidData>, API_ERROR>? = null
        var cur_search: Pair<ArrayList<VidData>, API_ERROR>?   = null

        // Remember last video
        var cur_playing: VidInfo? = null

        // Remember last view
        var cur_view: ViewSection? = null

        // Simple go back stack
        var back_view: Section? = null

        // Search is active
        var search: String? = null
    }

    enum class Section {
        POPULAR,
        TRENDING,
        SEARCH
    }

    enum class API_ERROR {
        OK,
        ERROR_CONNECTION,
        ERROR_API
    }

    enum class WindowSizeClass {
        PHONE,
        TABLET
    }

    data class ViewSection(
        var view: RecyclerView,
        var section: Section,
        var search: String = ""
    )

    data class VidInfo(
        val title: String,
        val id: String
    )

    // Get device type
    private lateinit var deviceType: WindowSizeClass

    private var searchItem: MenuItem? = null

    val vid_click: ((VidHolder?) -> Unit) = { id_val ->
        var id = id_val?.let {
            if (cur_playing?.id == id_val.id ) {
                ""
            } else {
                cur_playing = VidInfo(id_val.title.text.toString(), id_val.id)
                id_val.id
            }
        } ?: run {
            ""
        }

        val playerView = findViewById<View>(R.id.video_player)
        if (id != "") {
            VideoPlayer.exoPlayer?.release()
            pip_player.visibility = View.INVISIBLE
        } else {
            playerView.video_player.player = null
            pip_player.visibility = View.INVISIBLE
        }
        var view = Intent(this, VideoPlayer::class.java)
        view.putExtra("id", id)
        startActivityForResult(view, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 1) {
            return
        }
        minimized_video_playback()
    }

    private fun minimized_video_playback() {
        var playerView = findViewById<View>(R.id.video_player)
        playerView.video_player.player = VideoPlayer.exoPlayer
        change_play_status(VideoPlayer.exoPlayer?.isPlaying!!)
        VideoPlayer.exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                change_play_status(isPlaying)
            }
        })
        currently_playing.text = cur_playing?.title
        pip_player.visibility  = View.VISIBLE
    }

    private fun change_play_status(isPlaying: Boolean) {
        if (isPlaying) {
            exo_pause_min.visibility = View.VISIBLE
            exo_play_min.visibility  = View.INVISIBLE
        } else {
            exo_pause_min.visibility = View.INVISIBLE
            exo_play_min.visibility  = View.VISIBLE
        }
    }

    fun update_view(section: Section, search: String = "") : Pair<ArrayList<VidData>, API_ERROR> {
        var vid_list = ArrayList<VidData>()

        val resp: Response<List<Video>>
        try {
            resp = when (section) {
                Section.POPULAR -> client
                    .get_popular()
                    .execute()
                Section.TRENDING -> client
                    .get_trending()
                    .execute()
                Section.SEARCH -> client
                    .get_search(search)
                    .execute()
            }
        } catch (e: Exception) {
            return Pair(vid_list, API_ERROR.ERROR_CONNECTION)
        }

        if (resp.isSuccessful) {
            var msg = resp.body()!!
            for (vid in msg) {
                for (img_prev in vid.img_prev) {
                    if (img_prev.width == 1280) {
                        vid_list.add(VidData(img_prev.url, vid.title, vid.author, vid.views, vid.time, vid.len_secs, vid.id))
                        break
                    }
                }
            }
        } else {
            return Pair(vid_list, API_ERROR.ERROR_API)
        }

        return Pair(vid_list, API_ERROR.OK)
    }

    fun update_section_view(section: Section, refresh: Boolean = false) {
        if (refresh) {
            runOnUiThread {
                when (section) {
                    Section.POPULAR  -> {
                        cur_popular          = null
                        popular_view.adapter = VidRecycler(listOf(), application, vid_click)
                    }
                    Section.TRENDING -> {
                        cur_trending          = null
                        trending_view.adapter = VidRecycler(listOf(), application, vid_click)
                    }
                    Section.SEARCH   -> {
                        cur_search            = null
                        search_view.adapter   = VidRecycler(listOf(), application, vid_click)
                    }
                }
            }
        }

        val ret = when (section) {
            Section.POPULAR -> if (refresh || cur_popular == null) {
                cur_popular = update_view(section)
                cur_popular!!
            } else {
                cur_popular!!
            }
            Section.TRENDING -> if (refresh || cur_trending == null) {
                cur_trending = update_view(section)
                cur_trending!!
            } else {
                cur_trending!!
            }
            Section.SEARCH -> if (refresh || cur_search == null) {
                cur_search = update_view(section, cur_view?.search!!)
                cur_search!!
            } else {
                cur_search!!
            }
        }

        val adapter = VidRecycler(ret.first, application, vid_click)
        runOnUiThread {
            when (ret.second) {
                API_ERROR.ERROR_CONNECTION -> {
                    no_internet_main.visibility = View.VISIBLE
                }
                API_ERROR.ERROR_API -> {
                }
                API_ERROR.OK -> {
                    no_internet_main.visibility = View.INVISIBLE
                    val view = when (section) {
                        Section.SEARCH -> search_view
                        Section.POPULAR -> popular_view
                        Section.TRENDING -> trending_view
                    }

                    view.visibility    = View.VISIBLE
                    view.adapter       = adapter
                    set_view_layout(view)
                }
            }
        }
    }

    fun set_hooks() {
        val try_again_button = no_internet_main.findViewById<Button>(R.id.try_again_button)
        try_again_button.setOnClickListener {
            refresh.isRefreshing = true
            Thread(Runnable {
                cur_view?.let {
                    update_section_view(it.section, true)
                }
                runOnUiThread {
                    refresh.isRefreshing = false
                }
            }).start()
        }


        bottom_nav_view.setOnItemSelectedListener { item ->
            val next_view = when (item.itemId) {
                R.id.nav_popular -> Pair(popular_view, Section.POPULAR)
                else             -> Pair(trending_view, Section.TRENDING)
            }

            // Hide error view if there was ever one
            no_internet_main.visibility = View.INVISIBLE

            back_view?.let {
                back_view = next_view.second
                searchItem?.collapseActionView()
                return@setOnItemSelectedListener true
            }

            if (next_view.first.isEmpty()) {
                // Show progress wheel
                refresh.isRefreshing = true

                // Start loading
                cur_view?.let {
                    it.view.visibility = View.INVISIBLE
                    it.view            = next_view.first
                    it.section         = next_view.second
                }

                Thread(Runnable {
                    update_section_view(cur_view?.section!!)
                    runOnUiThread {
                        cur_view?.let {
                            refresh.isRefreshing = false
                            it.view.stopScroll()
                            it.view.layoutManager?.scrollToPosition(0)
                            it.view.visibility = View.VISIBLE
                        }
                    }
                }).start()
            } else {
                // Section already loaded
                cur_view?.let {
                    it.view.visibility = View.INVISIBLE
                    it.view            = next_view.first
                    it.view.stopScroll()
                    it.view.layoutManager?.scrollToPosition(0)
                    it.section = next_view.second
                    it.view.visibility = View.VISIBLE
                }
            }
            true
        }

        bottom_nav_view.setOnItemReselectedListener { item ->
            // Go to top when we reselect current view
            back_view?.let {
                back_view = when (item.itemId) {
                    R.id.nav_popular -> Section.POPULAR
                    else             -> Section.TRENDING
                }
                searchItem?.collapseActionView()
            } ?:run {
                cur_view?.let {
                    it.view.stopScroll()
                    it.view.layoutManager?.scrollToPosition(0)
                }
            }
        }

        exo_pause_min.setOnClickListener {
            VideoPlayer.exoPlayer?.pause()
        }

        exo_play_min.setOnClickListener {
            VideoPlayer.exoPlayer?.play()
        }

        exo_close_min.setOnClickListener {
            VideoPlayer.exoPlayer?.release()
            cur_playing           = null
            pip_player.visibility = View.INVISIBLE
        }

        refresh.setColorSchemeResources(R.color.progress_wheel);
        refresh.setOnRefreshListener(OnRefreshListener {
            // Refresh when refresh wheel is dragged down
            Thread(Runnable {
                cur_view?.let {
                    update_section_view(it.section, true)
                }
                runOnUiThread {
                    refresh.isRefreshing = false
                }
            }).start()
        })

        video_player_holder.setOnClickListener {
            vid_click(null)
        }

        var playerView = findViewById<View>(R.id.video_player)
        playerView.video_player.useController = false
        playerView.video_player.resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FIT

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)

        searchItem          = menu?.findItem(R.id.action_search)
        var searchView      = searchItem?.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE;

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        val suggestionProvider = SearchRecentSuggestions(
            this,
            SearchSuggestHistory.AUTHORITY,
            SearchSuggestHistory.MODE
        )

        // search queryTextChange Listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(search: String?): Boolean {
                search?.let {
                    if (it != "") {
                        // Save search to history
                        suggestionProvider.saveRecentQuery(it, null)

                        searchView.clearFocus()
                        refresh.isRefreshing = true
                        Thread(Runnable {
                            cur_view?.let { view ->
                                if (view.section != Section.SEARCH) {
                                    view.view.visibility = View.INVISIBLE
                                    back_view            = cur_view?.section
                                }
                            }
                            cur_view = ViewSection(search_view, Section.SEARCH, search = search)
                            update_section_view(Section.SEARCH, refresh = true)
                            runOnUiThread {
                                cur_view?.view?.visibility = View.VISIBLE
                                refresh.isRefreshing = false
                            }
                        }).start()
                        return false
                    }
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    search = query
                }
                return true
            }
        })

        // Expand Collapse listener
        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                search = null
                go_back()
                return true
            }

            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }
        })

        search?.let {
            searchItem?.expandActionView()
            searchView.setQuery(it, false)
        }

        return super.onCreateOptionsMenu(menu)
    }

    fun set_view_layout(view: RecyclerView) {
        when (deviceType) {
            WindowSizeClass.PHONE -> view.layoutManager = LinearLayoutManager(this)
            WindowSizeClass.TABLET -> {
                if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    view.layoutManager = GridLayoutManager(this, 2)
                    view.addItemDecoration(RecycleViewItemDecoration(2, 50, true))
                } else {
                    view.layoutManager = GridLayoutManager(this, 3)
                    view.addItemDecoration(RecycleViewItemDecoration(3, 50, true))
                }
            }
        }
    }

    fun go_back() {
        cur_view?.let {
            if (it.section != Section.SEARCH) {
                return
            }
        }
        back_view?.let {
            // Going back when collapsing search bar
            val view = when(back_view!!) {
                Section.SEARCH -> search_view
                Section.TRENDING -> trending_view
                Section.POPULAR -> popular_view
            }
            view.visibility = View.INVISIBLE
            cur_view?.view?.adapter    = VidRecycler(listOf(), application, vid_click)
            cur_view?.view?.visibility = View.INVISIBLE
            cur_view = ViewSection(view, back_view!!)
            cur_view?.view?.visibility = View.VISIBLE
            refresh.isRefreshing = true
            Thread(Runnable {
                update_section_view(back_view!!)
                refresh.isRefreshing = false
                back_view = null
            }).start()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When a menu item is selected
        when (item.itemId) {
            R.id.action_search -> {}
            R.id.action_settings -> {
                val view = Intent(this, SettingsActivity::class.java)
                startActivity(view)
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceType = computeWindowSizeClasses()

        requestedOrientation = if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        val dialog = Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logo)
        dialog.setCancelable(true)
        dialog.show()

        Thread(Runnable {
            cur_view?.let {
                cur_view = when (it.section) {
                    Section.POPULAR  -> ViewSection(popular_view, Section.POPULAR)
                    Section.TRENDING -> ViewSection(trending_view, Section.TRENDING)
                    Section.SEARCH   -> ViewSection(search_view, Section.SEARCH)
                }
            } ?:run {
                cur_view = ViewSection(popular_view, Section.POPULAR)
            }
            update_section_view(cur_view?.section!!)
            runOnUiThread {
                set_hooks()
                dialog.dismiss()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                cur_playing?.let { minimized_video_playback() }
            }
        }).start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main)
        }
    }

    override fun onBackPressed() {
        searchItem?.collapseActionView()
    }

    private fun computeWindowSizeClasses() : WindowSizeClass {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        val widthDp = metrics.bounds.width() /
                resources.displayMetrics.density
        return when {
            widthDp < 600f -> WindowSizeClass.PHONE
            else -> WindowSizeClass.TABLET
        }
    }
}