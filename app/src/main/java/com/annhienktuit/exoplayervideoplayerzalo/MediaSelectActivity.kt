package com.annhienktuit.exoplayervideoplayerzalo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.annhienktuit.exoplayervideoplayerzalo.adapters.MediaItemAdapter
import com.annhienktuit.exoplayervideoplayerzalo.models.Song
import com.android.volley.VolleyError

import org.json.JSONException

import org.json.JSONObject

import org.json.JSONArray

import com.android.volley.toolbox.JsonArrayRequest




class MediaSelectActivity : AppCompatActivity() {
    var requestURL = "https://61a03c9da6470200176132f7.mockapi.io/api/v1/Song"
    lateinit var queue:RequestQueue
    private lateinit var rcvMediaList: RecyclerView
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MediaItemAdapter.ViewHolder>? = null
    lateinit var songList:ArrayList<Song>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_select)
        songList = ArrayList()
        queue = Volley.newRequestQueue(this)
        rcvMediaList = findViewById<RecyclerView>(R.id.rcvMediaList)
        layoutManager = LinearLayoutManager(this)
        rcvMediaList.layoutManager = layoutManager
        sendRequest()
    }

    private fun sendRequest() {
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, requestURL, null,
            { response ->
                for (i in 0 until response.length()) {
                    try {
                        val jsonObject = response.getJSONObject(i)
                        var song: Song = Song(jsonObject.getString("id"),
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
            }
        ) { error -> Log.i("Volley Error: ", error.toString()) }
        queue.add(jsonArrayRequest)
    }
}