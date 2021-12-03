package com.annhienktuit.exoplayervideoplayerzalo.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.exoplayervideoplayerzalo.activities.PlayerActivity
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.models.Song
import com.annhienktuit.exoplayervideoplayerzalo.views.CircularImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import wseemann.media.FFmpegMediaMetadataRetriever

class MediaItemAdapter(context: Context, songList: List<Song>) :
    RecyclerView.Adapter<MediaItemAdapter.ViewHolder>() {
    private var mContext:Context = context
    private var mediaList:List<Song> = songList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_item_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var song = mediaList[position]
        holder.itemView.tag = mediaList[position]
        holder.mediaTitle.text = song.songName
        holder.mediaArtist.text = song.artist
        val retriever = FFmpegMediaMetadataRetriever()
        retriever.setDataSource(mediaList[position].url)
        val artwork = retriever.embeddedPicture //byteArray
        Glide.with(mContext).load(artwork).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.mediaArt)
        retriever.release()
//        val bitmap = BitmapFactory.decodeByteArray(artwork,0,artwork.size)
//        holder.mediaArt.setImageBitmap(Bitmap.createScaledBitmap(bitmap,200,200, false))
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mediaArtist: TextView
        var mediaTitle:TextView
        var mediaArt:CircularImageView
        var arrayURL = ArrayList<String>()
        var arrayTitle= ArrayList<String>()
        var arrayArtist = ArrayList<String>()
        var arrayID = ArrayList<String>()
        init {
            mediaTitle = itemView.findViewById(R.id.media_title)
            mediaArtist = itemView.findViewById(R.id.media_artist)
            mediaArt = itemView.findViewById(R.id.media_thumbnail)
            for(media in mediaList){
                arrayID.add(media.id)
                arrayURL.add(media.url)
                arrayTitle.add(media.songName)
                arrayArtist.add(media.artist)
            }
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                val currentMediaURL = mediaList[position].url
                val intent = Intent(itemView.context, PlayerActivity::class.java)
                intent.putExtra("url",currentMediaURL)
                intent.putExtra("index",position)
                intent.putExtra("listUrl",arrayURL)
                intent.putExtra("listTitle",arrayTitle)
                intent.putExtra("listArtist",arrayArtist)
                intent.putExtra("listID",arrayID)
                itemView.context.startActivity(intent)
            }
        }
    }
}
