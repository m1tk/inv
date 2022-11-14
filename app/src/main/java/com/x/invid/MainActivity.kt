package com.x.invid

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    enum class API_ERROR {
        OK,
        ERROR_CONNECTION,
        ERROR_API
    }

    var cur_view: RecyclerView? = null

    val vid_click: ((String) -> Unit) = { id ->
        var view = Intent(this, VideoPlayer::class.java)
        view.putExtra("id", id)
        startActivity(view)
    }

    fun update_view() : Pair<ArrayList<VidData>, API_ERROR> {
        var client = ClientBuilder()
            .get_client()

        var vid_list = ArrayList<VidData>()

        val resp: Response<List<Video>>
        try {
            resp = client
                .get_popular()
                .execute()
        } catch (e: Exception) {
            return Pair(vid_list, API_ERROR.ERROR_CONNECTION)
        }

        if (resp.isSuccessful) {
            var msg = resp.body()!!
            for (vid in msg) {
                for (img_prev in vid.img_prev) {
                    if (img_prev.width == 1280) {
                        vid_list.add(VidData(img_prev.url, vid.title, vid.author, vid.views.toString(), vid.time, vid.len_secs, vid.id))
                        break
                    }
                }
            }
        } else {
            return Pair(vid_list, API_ERROR.ERROR_API)
        }

        return Pair(vid_list, API_ERROR.OK)
    }

    fun set_hooks() {
        val try_again_button = no_internet_main.findViewById<Button>(R.id.try_again_button)
        try_again_button.setOnClickListener {
            Thread(Runnable {
                val ret = update_view()

                val adapter = VidRecycler(ret.first, application, vid_click)
                runOnUiThread {
                    println("wooonoooooo ${it.id}")
                    if (ret.second == API_ERROR.ERROR_API) {
                    } else if (ret.second == API_ERROR.OK) {
                        no_internet_main.visibility = View.INVISIBLE
                        popular_view.adapter = adapter
                        popular_view.layoutManager = LinearLayoutManager(this)
                    }
                }
            }).start()
        }

        bottom_nav_view.setOnItemSelectedListener {
            println("wooo ${it.itemId}")
            val other_view = if (it.itemId.equals("popular")) {
                popular_view
            } else {
                trending_view
            }
            if (other_view.isEmpty()) {
                // Start loading
            } else {
                
            }
            true
        }

        bottom_nav_view.setOnItemReselectedListener {
            // Do nothing
            // Prevent from reloading multiple times
        }
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
            val ret = update_view()

            val adapter = VidRecycler(ret.first, application, vid_click)
            runOnUiThread {
                dialog.dismiss()

                set_hooks()
                if (ret.second == API_ERROR.ERROR_CONNECTION) {
                    no_internet_main.visibility = View.VISIBLE
                } else if (ret.second == API_ERROR.ERROR_API) {
                } else {
                    cur_view                   = popular_view
                    popular_view.adapter       = adapter
                    popular_view.layoutManager = LinearLayoutManager(this)
                }
            }
        }).start()
    }
}