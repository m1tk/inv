package com.x.invid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.video_player.view.*


class VideoPlayer : AppCompatActivity() {
    var exoPlayer: SimpleExoPlayer?

    init {
        exoPlayer = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        val id: String? = intent.extras?.getString("id")

        exoPlayer      = SimpleExoPlayer.Builder(this).build()
        var playerView = findViewById<View>(R.id.video_player)
        playerView.video_player.player     = exoPlayer
        val mediaItem: MediaItem =
            MediaItem.fromUri("https://rr5---sn-p5qlsny6.googlevideo.com/videoplayback?expire=1668395946&ei=Sl9xY7nUBs6K8wSq1aCQAw&ip=15.204.174.205&id=o-AHZhcGVY50WOZvUoKweHopSqvmnS45nVMz6sm-lEAbrk&itag=22&source=youtube&requiressl=yes&mh=zm&mm=31%2C26&mn=sn-p5qlsny6%2Csn-t0a7sn7d&ms=au%2Conr&mv=m&mvi=5&pl=25&initcwndbps=303750&spc=SFxXNhneqihIYrlKX483fOKz-pgFdh4&vprv=1&svpuc=1&mime=video%2Fmp4&cnr=14&ratebypass=yes&dur=263.755&lmt=1668257234980015&mt=1668373975&fvip=1&fexp=24001373%2C24007246&c=ANDROID&txp=5532434&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Csvpuc%2Cmime%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIhAMYqEwYXqtTV1vruYD77Y3nZF876RD_et4jUHzC9Y4uBAiB7ECmg6LTeahMF7y0sWkaiiqY7yhupzEztXtatYt3tig%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAIWQlBCAoHxOfdCrCydQYpXNjwSr1FAGGkJd7tXCR2nnAiEAp0Ju3YvAEPqSwRZq4nbuYZ96vm8AARJlYZAuCNzPSIs%3D&host=rr5---sn-p5qlsny6.googlevideo.com")
        exoPlayer?.addMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.setPlayWhenReady(true)
    }

    override fun onStop() {
        super.onStop()
        //exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //exoPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        //super.enterPictureInPictureMode()
        //exoPlayer?.pause()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
    }

    override fun onBackPressed() {
        enterPictureInPictureMode()

    }
}