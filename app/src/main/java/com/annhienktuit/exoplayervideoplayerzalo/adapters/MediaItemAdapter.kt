package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.exoplayervideoplayerzalo.MainActivity
import com.annhienktuit.exoplayervideoplayerzalo.PlayerActivity
import com.annhienktuit.exoplayervideoplayerzalo.R

class MediaItemAdapter: RecyclerView.Adapter<MediaItemAdapter.ViewHolder>() {
    private var arrayTitle = arrayOf("Dizzy Cat", "Dizzy Dog", "Google","Earth", "Big Buck Bunnie")
    private var arrayURL = arrayOf("https://bestvpn.org/html5demos/assets/dizzy.mp4",
        "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4",
        "https://download.samplelib.com/mp3/sample-15s.mp3",
        "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4",
        "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4"
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
                Toast.makeText(itemView.context, "Playing $mediaName",Toast.LENGTH_SHORT).show()
                val intent = Intent(itemView.context, PlayerActivity::class.java)
                intent.putExtra("url",mediaName)
                intent.putExtra("index",position)
                intent.putExtra("listUrl",arrayURL)
                itemView.context.startActivity(intent)
            }
        }
    }
}
