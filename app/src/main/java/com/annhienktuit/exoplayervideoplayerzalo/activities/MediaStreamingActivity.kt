package com.annhienktuit.exoplayervideoplayerzalo.activities

import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.services.MusicService
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import com.annhienktuit.exoplayervideoplayerzalo.views.CircularImageView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView

class MediaStreamingActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private lateinit var tvSongName: TextView
    private lateinit var tvArtist: TextView
    private lateinit var imgArtwork: CircularImageView
    private lateinit var mediaTitleList: ArrayList<String>
    private lateinit var mediaURLList: ArrayList<String>
    private lateinit var mediaArtistList: ArrayList<String>
    private lateinit var mediaIDList: ArrayList<String>
    private var urlMedia:String = ""
    private var mediaTitle = ""
    private var artistTitle= ""
    var currentWindow = 0
    private lateinit var anim: ObjectAnimator
    private val connection = object: ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if(service is MusicService.MusicServiceBinder){
                playerView.player = service.getExoPlayerInstance()
                initializeAnimation()
                handleListener(service.getExoPlayerInstance())
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_streaming)
        checkPermissions()
        getItemsFromBundle()
        bindView()
        tvSongName.text = mediaTitleList[currentWindow]
        tvArtist.text = mediaArtistList[currentWindow]
        val intent = Intent(this, MusicService::class.java)
        sendDataToService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun getItemsFromBundle() {
        val extras:Bundle? = intent.extras
        if(extras != null){
            mediaTitleList = ArrayList()
            mediaArtistList = ArrayList()
            mediaIDList = ArrayList()
            urlMedia = extras.getString("url").toString()
            currentWindow = extras.getInt("index")
            mediaURLList = extras.getStringArrayList("listUrl") as ArrayList<String>
            mediaTitleList = extras.getStringArrayList("listTitle") as ArrayList<String>
            mediaArtistList = extras.getStringArrayList("listArtist") as ArrayList<String>
            mediaIDList = extras.getStringArrayList("listID") as ArrayList<String>
        }
    }

    private fun initializeAnimation(){
        anim= ObjectAnimator.ofFloat(imgArtwork, View.ROTATION, 0f, 360f).setDuration(20000)
        anim.repeatCount = Animation.INFINITE
        anim.interpolator = LinearInterpolator()
        anim.start()
    }

    private fun handleListener(exoPlayer: ExoPlayer){
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    anim.resume()
                } else if (playWhenReady) {
                    anim.pause()
                } else {
                    anim.pause()
                }
            }
            //TODO: extract to get from metadata
            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
                super.onTracksChanged(trackGroups, trackSelections)
                for(idx in 0 until trackGroups?.length!!){
                    val trackGroup = trackGroups.get(idx)
                    for(j in 0 until trackGroup.length){
                        val trackMetaData = trackGroup.getFormat(j).metadata
                        for(k in 0 until trackMetaData!!.length()){
                            val songNameMetadataEntry = trackMetaData!!.get(k)
                            if(songNameMetadataEntry is TextInformationFrame && songNameMetadataEntry.id == "TIT2") {
                                mediaTitle = songNameMetadataEntry.value
                                tvSongName.text = mediaTitle
                                break
                            }
                            else tvSongName.text = mediaTitleList[exoPlayer.currentWindowIndex]
                        }
                        for(k in 0 until trackMetaData!!.length()) {
                            val artistMetadataEntry = trackMetaData!!.get(k)
                            if (artistMetadataEntry is TextInformationFrame && artistMetadataEntry.id == "TPE1") {
                                artistTitle = artistMetadataEntry.value
                                tvArtist.text = artistTitle
                                break
                            } else tvArtist.text = mediaArtistList[exoPlayer.currentWindowIndex]
                        }
                    }
                }
            }
        })
    }

    fun sendDataToService(intent: Intent){
        intent.putExtra("url",urlMedia)
        intent.putExtra("index",currentWindow)
        intent.putExtra("listUrl",mediaURLList)
        intent.putExtra("listTitle",mediaTitleList)
        intent.putExtra("listArtist",mediaArtistList)
        intent.putExtra("listID",mediaIDList)
    }

    private fun bindView() {
        playerView = findViewById(R.id.hls_player_view)
        imgArtwork = findViewById(R.id.exo_artwork)
        tvArtist = findViewById(R.id.tvSongArtist)
        tvSongName = findViewById(R.id.tvSongTitle)
    }
}