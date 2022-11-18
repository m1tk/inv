package com.x.invid

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_player.view.*
import retrofit2.Response
import com.x.invid.api.ClientBuilder
import com.x.invid.api.Video
import com.x.invid.adapter.VidHolder
import com.x.invid.adapter.VidRecycler
import com.x.invid.model.VidData

class MainActivity : AppCompatActivity() {
    companion object {
        var client = ClientBuilder()
            .get_client()

        // Remember objects of views
        var cur_popular: Pair<ArrayList<VidData>, API_ERROR>?  = null
        var cur_trending: Pair<ArrayList<VidData>, API_ERROR>? = null

        // Remember last video
        var cur_playing: VidInfo? = null

        // Remember last view
        var cur_view: ViewSection? = null
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

    data class ViewSection(
        var view: RecyclerView,
        var section: Section
    )

    data class VidInfo(
        val title: String,
        val id: String
    )

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

    fun update_section_view(section: Section) {
        val ret = when (section) {
            Section.POPULAR -> cur_popular?.let {
                cur_popular!!
            } ?:run {
                cur_popular = update_view(section)
                cur_popular!!
            }
            Section.TRENDING -> cur_trending?.let {
                cur_trending!!
            } ?:run {
                cur_trending = update_view(section)
                cur_trending!!
            }
            Section.SEARCH -> update_view(section)
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
                    cur_view?.let {
                        it.view.adapter = adapter
                        it.view.layoutManager = LinearLayoutManager(this)
                    }
                }
            }
        }
    }

    fun set_hooks() {
        val try_again_button = no_internet_main.findViewById<Button>(R.id.try_again_button)
        try_again_button.setOnClickListener {
            Thread(Runnable {
                update_section_view(cur_view?.section!!)
            }).start()
        }

        bottom_nav_view.setOnItemSelectedListener { item ->
            val next_view = when (item.itemId) {
                R.id.nav_popular -> Pair(popular_view, Section.POPULAR)
                else              -> Pair(trending_view, Section.TRENDING)
            }

            if (next_view.first.isEmpty()) {
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
                    it.view    = next_view.first
                    it.view.stopScroll()
                    it.view.layoutManager?.scrollToPosition(0)
                    it.section = next_view.second
                    it.view.visibility = View.VISIBLE
                }
            }
            true
        }

        bottom_nav_view.setOnItemReselectedListener {
            // Do nothing
            // Prevent from reloading multiple times
        }

        exo_pause_min.setOnClickListener {
            VideoPlayer.exoPlayer?.pause()
        }

        exo_play_min.setOnClickListener {
            VideoPlayer.exoPlayer?.play()
        }

        exo_close_min.setOnClickListener {
            VideoPlayer.exoPlayer?.release()
            pip_player.visibility = View.INVISIBLE
        }

        refresh.setColorSchemeResources(R.color.progress_wheel);
        refresh.setOnRefreshListener(OnRefreshListener {
            println("nooooooorefreshshshsh")
            refresh.setRefreshing(false)
        })

        video_player_holder.setOnClickListener {
            vid_click(null)
        }

        var playerView = findViewById<View>(R.id.video_player)
        playerView.video_player.useController = false
        playerView.video_player.resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dialog = Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.logo)
        dialog.setCancelable(true)
        dialog.show()

        Thread(Runnable {
            cur_view = ViewSection(popular_view, Section.POPULAR)
            update_section_view(Section.POPULAR)
            runOnUiThread {
                set_hooks()
                dialog.dismiss()
                cur_playing?.let { minimized_video_playback() }
            }
        }).start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_main)
    }
}