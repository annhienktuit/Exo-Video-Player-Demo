package com.annhienktuit.exoplayervideoplayerzalo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.exoplayervideoplayerzalo.adapters.MediaItemAdapter

class MediaSelectActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<MediaItemAdapter.ViewHolder>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_select)
        val rcvMediaList = findViewById<RecyclerView>(R.id.rcvMediaList)
        layoutManager = LinearLayoutManager(this)
        rcvMediaList.layoutManager = layoutManager
        adapter = MediaItemAdapter()
        rcvMediaList.adapter = adapter

    }
}