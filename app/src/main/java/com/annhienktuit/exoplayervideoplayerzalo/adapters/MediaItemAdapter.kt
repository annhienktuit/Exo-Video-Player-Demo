package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.exoplayervideoplayerzalo.PlayerActivity
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.models.Song

class MediaItemAdapter(context: Context, songList: List<Song>) :
    RecyclerView.Adapter<MediaItemAdapter.ViewHolder>() {
    private var mContext:Context = context
    private var mediaList:List<Song> = songList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tag = mediaList[position]
        var song = mediaList[position]
        holder.mediaTitle.text = song.songName
        holder.mediaURL.text = song.url
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mediaURL: TextView
        var mediaTitle:TextView
        var arrayURL = ArrayList<String>()
        var arrayTitle= ArrayList<String>()
        init {
            mediaTitle = itemView.findViewById(R.id.media_title)
            mediaURL = itemView.findViewById(R.id.media_url)
            for(media in mediaList){
                arrayURL.add(media.url)
                arrayTitle.add(media.songName)
            }
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                val currentMediaURL = mediaList[position].url
                val intent = Intent(itemView.context, PlayerActivity::class.java)
                intent.putExtra("url",currentMediaURL)
                intent.putExtra("index",position)
                intent.putExtra("listUrl",arrayURL)
                intent.putExtra("listTitle",arrayTitle)
                itemView.context.startActivity(intent)
            }
        }
    }
}
