package com.annhienktuit.exoplayervideoplayerzalo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.annhienktuit.exoplayervideoplayerzalo.adapters.MediaItemAdapter
import com.annhienktuit.exoplayervideoplayerzalo.models.Song
import com.android.volley.toolbox.*
import com.annhienktuit.exoplayervideoplayerzalo.R

import org.json.JSONException

import com.annhienktuit.exoplayervideoplayerzalo.utils.Constants
import com.facebook.shimmer.ShimmerFrameLayout

class MediaSelectActivity : AppCompatActivity() {
    var requestURL = Constants.requestURL
    lateinit var queue:RequestQueue
    lateinit var requestQueue:RequestQueue
    private lateinit var rcvMediaList: RecyclerView
    private lateinit var shimmerFrameLayout:ShimmerFrameLayout
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MediaItemAdapter.ViewHolder>? = null
    lateinit var songList:ArrayList<Song>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_select)
        songList = ArrayList()
        val cache = DiskBasedCache(cacheDir, 1024*1024)
        val network =  BasicNetwork(HurlStack())
        requestQueue = RequestQueue(cache, network).apply {
            start()
        }
        queue = Volley.newRequestQueue(this)
        rcvMediaList = findViewById(R.id.rcvMediaList)
        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout)
        shimmerFrameLayout.startShimmer()
        layoutManager = LinearLayoutManager(this)
        rcvMediaList.layoutManager = layoutManager
        sendRequest()
    }

    override fun onResume() {
        super.onResume()
        shimmerFrameLayout.startShimmer()
    }

    private fun sendRequest() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, requestURL, null,
            { response ->
                for (i in 0 until response.length()) {
                    try {
                        val jsonObject = response.getJSONObject(i)
                        var song = Song(
                            jsonObject.getString("id"),
                            jsonObject.getString("url"),
                            jsonObject.getString("song_name"),
                            jsonObject.getString("artist")
                        )
                        songList.add(song)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                adapter = MediaItemAdapter(this, songList)
                rcvMediaList.adapter = adapter
                shimmerFrameLayout.visibility = View.INVISIBLE
                rcvMediaList.visibility = View.VISIBLE
            }
        ) { error -> Log.e("Volley Error: ", error.toString()) }
        jsonArrayRequest.setShouldCache(true)
        jsonArrayRequest.headers
        requestQueue.add(jsonArrayRequest)
        requestQueue.cache.get(requestURL)
    }
}