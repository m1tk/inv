package com.x.invid.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.x.invid.MainActivity.Companion.client
import com.x.invid.R
import com.x.invid.api.VideoInfo
import kotlinx.android.synthetic.main.player_controllers.*
import kotlinx.android.synthetic.main.video_player.view.*
import retrofit2.Response


class VideoPlayer : AppCompatActivity() {
    companion object {
        var exoPlayer: SimpleExoPlayer? = null
    }
    private var isFullScreen: Boolean = false
    private var isLock: Boolean       = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullscreen()
        }

        if (intent.extras?.getString("id")!! != "") {
            exoPlayer = SimpleExoPlayer.Builder(this)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000)
                .build()
        }
        var playerView = findViewById<View>(R.id.video_player)
        playerView.video_player.player = exoPlayer

        // Keep screen on while playing
        playerView.video_player.player?.addListener(object : Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playerView.keepScreenOn = isPlaying
            }
        })

        set_listeners()
        start_player()
    }

    private fun set_listeners() {
        // Setting fullscreen
        imageViewFullScreen.setOnClickListener {
            fullscreen()
        }
        // Setting screenlock
        imageViewLock.setOnClickListener {
            if (!isLock) {
                imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_lock
                    )
                )
                if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            } else {
                imageViewLock.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_lock_open
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            isLock = !isLock
            if (isLock) {
                linearLayoutControlUp.visibility = View.INVISIBLE
                linearLayoutControlBottom.visibility = View.INVISIBLE
            } else {
                linearLayoutControlUp.visibility = View.VISIBLE
                linearLayoutControlBottom.visibility = View.VISIBLE
            }
        }
    }

    fun fullscreen() {
        if (!isFullScreen) {
            imageViewFullScreen.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    com.google.android.exoplayer2.R.drawable.exo_icon_fullscreen_exit
                )
            )

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            imageViewFullScreen.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.ic_fullscreen_open
                )
            )

            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        isFullScreen = !isFullScreen
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Auto rotate when screen is rotated
        if (!isLock) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fullscreen()
            } else {
                fullscreen()
            }
        }
    }

    fun start_player() {
        Thread(Runnable {
            if (intent.extras?.getString("id").isNullOrBlank()) {
                return@Runnable
            }
            var resp: Response<VideoInfo>?
            var url: String? = null
            var id           = intent.extras?.getString("id")!!
            try {
                resp = client
                    .get_video(id)
                    .execute()
            } catch (e: Exception) {
                println("wooo $e")
                resp = null
            }

            resp?.let {
                if (resp.isSuccessful) {
                    var msg = resp.body()!!
                    if (msg.streams.isEmpty()) {
                        on_fetch_stream_error()
                        return@Runnable
                    }
                    url = msg.streams[msg.streams.size-1].url
                }
            }

            runOnUiThread {
                println("wooo url $url")
                url?.let {
                    val mediaItem: MediaItem =
                        MediaItem.fromUri(it)
                    exoPlayer?.addMediaItem(mediaItem)
                    exoPlayer?.prepare()
                    exoPlayer?.setPlayWhenReady(true)
                } ?: run {
                    on_fetch_stream_error()
                }
            }
        }).start()
    }

    fun on_fetch_stream_error() {
        var toast = Toast.makeText(this, "Can't contact server, check connection or retry", Toast.LENGTH_LONG);
        toast.show();
    }
}