package com.annhienktuit.exoplayervideoplayerzalo

import android.app.NotificationManager
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.isLandscapeOrientation
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.SimpleCache

import android.app.NotificationChannel
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import com.annhienktuit.exoplayervideoplayerzalo.adapters.DescriptionAdapter
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheParams
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.splitSongName
import com.annhienktuit.exoplayervideoplayerzalo.utils.PreLoadingCache
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory


class PlayerActivity : AppCompatActivity() {
    private lateinit var exoPlayer:SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var tvPosition: TextView
    private lateinit var btnQuality: Button
    private lateinit var btnFullScr: Button
    private lateinit var btnMute: Button
    private lateinit var btnFilePicker: Button
    private lateinit var btnSpeed: Button
    private var urlMedia:String = ""
    private lateinit var mediaTitleList: ArrayList<String>
    private lateinit var mediaURLList: ArrayList<String>
    private lateinit var mediaSourceList:ArrayList<MediaSource>
    private lateinit var mediaArtistList: ArrayList<String>
    private lateinit var mediaIDList: ArrayList<String>
    var currentVolume = 0F
    var currentWindow = 0
    private var playbackParams = PlaybackParameters(1f)
    private val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE).build()
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams: DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().build()
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var loadControl:LoadControl
    private val simpleCache: SimpleCache = CacheUtils.simpleCache
    private lateinit var mediaMetadataRetriever: MediaMetadataRetriever
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        checkPermissions()
        val extras:Bundle? = intent.extras
        if(extras !=  null){
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
        bindView()
        initializePlayer()
        initializeNotification()
        createNotificationChannel()
        tvPosition.text = "00:00"
        btnQuality.visibility = View.GONE
        btnFilePicker.visibility = View.GONE
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
    }

    private fun initializePlayer(){
        initializeMedia()
        initializeLoadControl()
        val mediaSourceList = preparePlaylist()
        trackSelector.parameters = trackParams
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl)
        exoPlayer.playbackParameters = playbackParams
        exoPlayer.apply {
            prepare(mediaSourceList)
            seekTo(currentWindow, 1)
            playbackParameters = playbackParams
        }
        exoPlayer.playWhenReady = true
        playerView.player = exoPlayer
        playerView.keepScreenOn = true
    }

    private fun initializeNotification(){
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        mediaSession.isActive = true
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        val timelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(
                player: Player,
                windowIndex: Int
            ): MediaDescriptionCompat {
                player.let { safePlayer ->
                    return MediaDescriptionCompat.Builder().apply {
                        setTitle(mediaTitleList[windowIndex])
                        setDescription(mediaArtistList[windowIndex])
                    }.build()
                }
                return MediaDescriptionCompat.Builder().build()
            }
        }
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator)
        mediaSessionConnector.setPlayer(exoPlayer, null)
        playerNotificationManager = PlayerNotificationManager(this, channelID, notificationID,  DescriptionAdapter(mediaController))
        playerNotificationManager.apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setPlayer(exoPlayer)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_description)
            val descriptionText = "DESC"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeMedia() {
        httpDataSourceFactory = DefaultHttpDataSourceFactory("pre-cache")
        mediaDataSourceFactory = DefaultDataSourceFactory(this, httpDataSourceFactory
        ) //for local media
        cacheDataSourceFactory = CacheDataSourceFactory(simpleCache, httpDataSourceFactory)
    }

    private fun preparePlaylist():ConcatenatingMediaSource{
        mediaSourceList = ArrayList()
        for(i in 0 until mediaURLList.size){
            val preCaching = PreLoadingCache(this)
            var params = CacheParams(mediaIDList[i],mediaURLList[i])
            preCaching.execute(params)
            mediaSourceList.add(
                ExtractorMediaSource(
                    Uri.parse(mediaURLList[i]), cacheDataSourceFactory,
                    DefaultExtractorsFactory(),null,null,mediaIDList[i]))
        }

        val concatenatingMediaSource = ConcatenatingMediaSource()
        concatenatingMediaSource.addMediaSources(mediaSourceList)
        return concatenatingMediaSource
    }

    private fun initializeLoadControl(){
        DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true,C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                16*1024, 64*1024, 1024, 1024)
            .setTargetBufferBytes(DefaultLoadControl.DEFAULT_TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(DefaultLoadControl.DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS)
            .createDefaultLoadControl().also { loadControl = it }
    }

    private fun hideSystemUi() {
        //Handle fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun rotateScreen(){
        if(isLandscapeOrientation()){
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen)
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

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun onStart() {
        super.onStart()
        hideSystemUi()
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
    companion object {
        const val MEDIA_SESSION_TAG = "media_session"
        const val notificationID = 123
        const val channelID = "com.annhienktuit.exoplayervideoplayerzalo"
    }

}