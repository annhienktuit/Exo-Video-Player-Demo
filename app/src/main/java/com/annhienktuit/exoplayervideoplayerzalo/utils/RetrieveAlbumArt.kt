package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.annhienktuit.exoplayervideoplayerzalo.views.CircularImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.IOException


class RetrieveAlbumArtParams{
    var mContext:Context? = null
    var url:String? = null
    var imgView: CircularImageView? = null
    constructor(context:Context, url:String, imageView: CircularImageView){
        this.mContext = context
        this.url = url
        this.imgView = imageView
    }
}

class RetrieveAlbumArt() : AsyncTask<RetrieveAlbumArtParams, Void, ByteArray>() {
    private lateinit var mUrl:String
    private lateinit var mContext: Context
    private lateinit var imageView: CircularImageView
    override fun doInBackground(vararg params: RetrieveAlbumArtParams): ByteArray? {
        try {
            this.mUrl = params[0].url!!
            this.mContext = params[0].mContext!!
            this.imageView = params[0].imgView!!
            val retriever = FFmpegMediaMetadataRetriever()
            retriever.setDataSource(mUrl)
            return retriever.embeddedPicture
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: ByteArray?) {
        super.onPostExecute(result)
        Glide.with(mContext).load(result).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView)
    }

}