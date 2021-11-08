package com.annhienktuit.exoplayervideoplayerzalo

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils.Companion.simpleCache
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.isLandscapeOrientation
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.requestFilePermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class PlayerActivity : AppCompatActivity() {
    private lateinit var exoPlayer:SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var tvPosition: TextView
    private lateinit var btnQuality: Button
    private lateinit var btnFullScr: Button
    private lateinit var btnMute: Button
    private lateinit var btnFilePicker: Button
    private lateinit var btnSpeed: Button
    private lateinit var rlRes: RelativeLayout
    private var urlMedia:String = ""
    var currentVolume = 0F
    private var playbackParams = PlaybackParameters(1f)
    private val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE).build()
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams: DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().build()
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultMediaItem: MediaItem
    private lateinit var defaultMediaSource: ProgressiveMediaSource
    private val simpleCache: SimpleCache = CacheUtils.simpleCache
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        checkPermissions()
        var extras:Bundle? = intent.extras
        if(extras !=  null){
            urlMedia = extras.getString("url").toString()
            Log.i("extras: ", urlMedia)
        }
        bindView()
        initializePlayer()
        tvPosition.text = "00:00"
        btnQuality.visibility = View.GONE
        btnFullScr.setOnClickListener { rotateScreen() }
        btnSpeed.setOnClickListener { changeSpeed() }
        btnMute.setOnClickListener {
            currentVolume = exoPlayer.volume
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
            requestFilePermissions()
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            intent.setDataAndType(Uri.parse("/storage/emulated/0/"),"video/*")
            startActivityForResult(intent, MainActivity.OPEN_REQUEST_CODE)
        }
    }

    private fun initializePlayer(){
        initializeMedia()
        trackSelector.parameters = trackParams
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes,true)
            .build().apply {
                addMediaSource(defaultMediaSource)
                addMediaItem(MediaItem.fromUri(getString(R.string.music_mp3)))
                playbackParameters = playbackParams
                setWakeMode(C.WAKE_MODE_NETWORK)
                playWhenReady = true
                prepare()
            }
        playerView.player = exoPlayer
        playerView.keepScreenOn = true
    }

    private fun initializeMedia() {
        defaultMediaItem = MediaItem.fromUri(urlMedia)
        httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        mediaDataSourceFactory = DefaultDataSourceFactory(this,
            httpDataSourceFactory
        ) //for local media
        cacheDataSourceFactory = CacheDataSource.Factory() //for online media
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        defaultMediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(defaultMediaItem)
    }


    private fun rotateScreen(){
        if(isLandscapeOrientation()){
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen_skrink)
        }
        else {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen_skrink)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }


    private fun changeSpeed() {
        if(playbackParams.speed == 1f){
            playbackParams = PlaybackParameters(2f)
            toast("Playback speed 2x")
        }
        else {
            playbackParams = PlaybackParameters(1f)
            toast("Playback speed 1x")
        }
        exoPlayer.playbackParameters = playbackParams
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exoPlayer.release()
    }

    private fun bindView() {
        playerView = findViewById(R.id.player_view)
        tvPosition = findViewById(R.id.exo_position)
        btnQuality = findViewById(R.id.exo_quality_icon)
        btnFullScr = findViewById(R.id.exo_fullscreen_icon)
        btnMute = findViewById(R.id.exo_mute)
        btnFilePicker = findViewById(R.id.exo_file_picker)
        btnSpeed = findViewById(R.id.exo_playback_speed)
    }

}