package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.exoplayervideoplayerzalo.PlayerActivity
import com.annhienktuit.exoplayervideoplayerzalo.R
import java.lang.Exception

class MediaItemAdapter: RecyclerView.Adapter<MediaItemAdapter.ViewHolder>() {
    private var arrayTitle = arrayOf("Dizzy Cat", "Dizzy Dog", "Google","Long mp3","Earth", "Big Buck Bunnie","We are the people","Test Meta")
    private var arrayURL = arrayOf("https://bestvpn.org/html5demos/assets/dizzy.mp4",
        "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4",
        "https://download.samplelib.com/mp3/sample-15s.mp3",
        "https://filesamples.com/samples/audio/mp3/Symphony%20No.6%20(1st%20movement).mp3",
        "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4",
        "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4",
        "https://data.chiasenhac.com/down2/2171/1/2170740-9d48456b/128/We%20Are%20The%20People%20-%20Martin%20Garrix_%20Bono.mp3",
        "https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_700KB.mp3",
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mediaTitle.text = arrayTitle[position]
        holder.mediaURL.text = arrayURL[position]
    }

    override fun getItemCount(): Int {
        return arrayTitle.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mediaURL: TextView
        var mediaTitle:TextView
        init {
            mediaTitle = itemView.findViewById(R.id.media_title)
            mediaURL = itemView.findViewById(R.id.media_url)
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                val mediaName = arrayURL[position]
                val intent = Intent(itemView.context, PlayerActivity::class.java)
                intent.putExtra("url",mediaName)
                intent.putExtra("index",position)
                intent.putExtra("listUrl",arrayURL)
                itemView.context.startActivity(intent)
            }
        }
    }
}
