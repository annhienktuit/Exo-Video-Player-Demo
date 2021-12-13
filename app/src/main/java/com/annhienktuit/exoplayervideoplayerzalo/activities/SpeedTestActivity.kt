package com.annhienktuit.exoplayervideoplayerzalo.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class SpeedTestActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var mediaSource: MediaSource
    private var startLoadinginMs: Long = 0L
    private var finishLoadinginMs: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_test)
        bindView()
        initializeMedia()
        initializePlayer()
    }
    private fun initializeMedia() {
        mediaSource = ExtractorMediaSource(Uri.parse("file:///android_asset/mp3/demo.mp3"),
            DefaultDataSourceFactory(this,"default"),
            DefaultExtractorsFactory(),
            null,
            null)
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,DefaultTrackSelector(),DefaultLoadControl())
        addListener()
        exoPlayer.playWhenReady = true
        exoPlayer.prepare(mediaSource)
        startLoadinginMs = System.currentTimeMillis()
        playerView.player = exoPlayer
    }

    private fun addListener() {
        exoPlayer.addListener(object: Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if(playbackState == Player.STATE_READY){
                    finishLoadinginMs = System.currentTimeMillis()
                    Log.i("Loading time is ",(finishLoadinginMs - startLoadinginMs).toString())
                }
            }
        })
    }

    private fun bindView() {
        playerView = findViewById(R.id.player_view)
    }
}