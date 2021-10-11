package com.annhienktuit.exoplayervideoplayerzalo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {
    private lateinit var player_view:PlayerView
    private lateinit var exoPlayer:SimpleExoPlayer
    private lateinit var loadControl:LoadControl
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams:DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().build()
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var mediaSourceFactory:MediaSourceFactory
    private lateinit var mediaSourceHighRes: ProgressiveMediaSource
    private lateinit var mediaSourceLowRes: ProgressiveMediaSource
    private lateinit var btnHighRes:Button
    private lateinit var btnLowRes:Button
    private lateinit var tvResolution:TextView
    private var playWhenReady = false
    private var currentWindow = 0
    private var playbackPosition = 0L
    private lateinit var mediaItemHigh:MediaItem
    private lateinit var mediaItemLow:MediaItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        initializePlayer()
        btnHighRes.setOnClickListener {
            exoPlayer.apply {
                playWhenReady = false
                playbackPosition = this.currentPosition
                currentWindow = this.currentWindowIndex
                println("$playbackPosition $currentWindow")
                setMediaSource(mediaSourceHighRes)
                seekTo(currentWindow, playbackPosition)
                playWhenReady = true
            }
            tvResolution.text = "High resolution"
        }
        btnLowRes.setOnClickListener {
            exoPlayer.apply {
                playWhenReady = false
                playbackPosition = this.currentPosition
                currentWindow = this.currentWindowIndex
                println("$playbackPosition $currentWindow")
                setMediaSource(mediaSourceLowRes)
                seekTo(currentWindow, playbackPosition)
                playWhenReady = true
            }
            tvResolution.text = "Low resolution"
        }
    }

    private fun hideSystemUi() {
        //Handle fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, player_view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun initializePlayer() {
        initializeMedia()
        initializeLoadControl()
        trackSelector.parameters = trackParams
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        exoPlayer.apply {
            addMediaSource(mediaSourceLowRes)
            playWhenReady = this.playWhenReady
            seekTo(currentWindow, playbackPosition)
            prepare()
        }
        player_view.requestFocus()
        player_view.player = exoPlayer
    }

    private fun initializeMedia() {
        mediaItemHigh = MediaItem.fromUri(getString(R.string.video_mp4_high))
        mediaItemLow = MediaItem.fromUri(getString(R.string.video_mp4_low))
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
        mediaSourceHighRes = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItemHigh)
        mediaSourceLowRes = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItemLow)
        mediaSourceFactory = DefaultMediaSourceFactory(mediaDataSourceFactory)

    }

    private fun initializeLoadControl(){
        DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true,C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                32*1024, 64*1024, 1024, 1024)
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS)
            .createDefaultLoadControl().also { loadControl = it }
    }

    private fun releasePlayer() {
        exoPlayer.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
        }
        exoPlayer.release()
    }

    private fun bindView() {
        player_view = findViewById(R.id.player_view)
        btnHighRes = findViewById(R.id.btnHighRes)
        btnLowRes = findViewById(R.id.btnLowRes)
        tvResolution = findViewById(R.id.tvRes)
    }
    //Handle lifecycle
    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            println("$currentWindow $playbackPosition")
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24)) {
            initializePlayer()
        }
    }
    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }


}