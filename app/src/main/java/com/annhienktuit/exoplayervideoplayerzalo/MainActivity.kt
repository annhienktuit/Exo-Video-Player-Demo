package com.annhienktuit.exoplayervideoplayerzalo

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.getRealPathFromURI
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.isLandscapeOrientation
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {
    val OPEN_REQUEST_CODE = 1
    private lateinit var player_view:PlayerView
    private lateinit var exoPlayer:SimpleExoPlayer
    private lateinit var loadControl:LoadControl
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams:DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().setMaxVideoSize(1920,1080).build()
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var mediaSourceFactory:MediaSourceFactory
    private lateinit var mediaSourceHighRes: ProgressiveMediaSource
    private lateinit var mediaSourceLowRes: ProgressiveMediaSource
    private lateinit var tvResolution:TextView
    private lateinit var tvPosition:TextView
    private lateinit var btnQuality:Button
    private lateinit var btnFullScr:Button
    private lateinit var btnMute:Button
    private lateinit var btnFilePicker:Button
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var currentVolume = 0F
    private var uriMedia:String? = ""
    private lateinit var mediaItemHigh:MediaItem
    private lateinit var mediaItemLow:MediaItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindView()
        initializePlayer()
        tvPosition.text = "00:00"
        btnQuality.setOnClickListener {
            val popupMenu = PopupMenu(this, btnQuality)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.action_highres -> switchHighRes()
                    else -> switchLowRes()
                }
                true
            }
            popupMenu.show()
        }
        btnFullScr.setOnClickListener {
            rotateScreen()
        }
        btnMute.setOnClickListener {
            currentVolume = exoPlayer?.volume
            if (currentVolume == 0f) {
                exoPlayer.volume = 1f
                btnMute.setBackgroundResource(R.drawable.ic_mute)
            }
            else {
                exoPlayer.volume = 0f
                btnMute.setBackgroundResource(R.drawable.ic_unmute)
            }
        }
        btnFilePicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            intent.setType("video/*")
            startActivityForResult(intent, OPEN_REQUEST_CODE)
        }
        exoPlayer.addAnalyticsListener(EventLogger(trackSelector))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === RESULT_OK && requestCode === OPEN_REQUEST_CODE) {
            val uri: Uri? = data?.getData()
            uriMedia = getRealPathFromURI(uri)
        }
        switchLocalFile()
    }

    private fun initializePlayer() {
        initializeMedia()
        initializeLoadControl()
        trackSelector.parameters = trackParams
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
            addMediaSource(mediaSourceLowRes)
            addMediaItem(MediaItem.fromUri(getString(R.string.music_mp3)))
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
        tvResolution = findViewById(R.id.tvRes)
        tvPosition = findViewById(R.id.exo_position)
        btnQuality = findViewById(R.id.exo_quality_icon)
        btnFullScr = findViewById(R.id.exo_fullscreen_icon)
        btnMute = findViewById(R.id.exo_mute)
        btnFilePicker = findViewById(R.id.exo_file_picker)
    }

    private fun switchHighRes(){
        exoPlayer.apply {
            playWhenReady = false
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            setMediaSource(mediaSourceHighRes)
            seekTo(currentWindow, playbackPosition)
            playWhenReady = true
        }
        tvResolution.text = "High resolution"
    }

    private fun switchLowRes(){
        exoPlayer.apply {
            playWhenReady = false
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            setMediaSource(mediaSourceLowRes)
            seekTo(currentWindow, playbackPosition)
            playWhenReady = true
        }
        tvResolution.text = "Low resolution"
    }

    private fun switchLocalFile(){
        if(uriMedia != null){
            exoPlayer.playWhenReady = false
            val mediaItemFromFile = MediaItem.fromUri(uriMedia!!)
            val newMediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItemFromFile)
            exoPlayer.setMediaSource(newMediaSource)
            exoPlayer.playWhenReady = true
            Log.i("picker: ",uriMedia.toString())
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

    private fun rotateScreen(){
        if(isLandscapeOrientation()){
            player_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            tvResolution.visibility = View.VISIBLE
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen_skrink)
        }
        else {
            player_view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            tvResolution.visibility = View.INVISIBLE
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen_skrink)
        }

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