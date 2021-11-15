package com.annhienktuit.exoplayervideoplayerzalo

import android.app.NotificationManager
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
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
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache

import android.app.NotificationChannel
import android.content.Context
import android.media.MediaMetadataRetriever
import android.support.v4.media.MediaDescriptionCompat
import com.annhienktuit.exoplayervideoplayerzalo.adapters.DescriptionAdapter
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import java.lang.Exception


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
    private lateinit var urlMediaList: Array<String>
    private lateinit var mediaSourceList:ArrayList<MediaSource>
    var currentVolume = 0F
    var currentWindow = 0
    private var playbackParams = PlaybackParameters(1f)
    private val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE).build()
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams: DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().build()
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultMediaItem: MediaItem
    private lateinit var defaultMediaSource: ProgressiveMediaSource
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
            urlMedia = extras.getString("url").toString()
            urlMediaList = extras.getStringArray("listUrl") as Array<String>
            currentWindow = extras.getInt("index")
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
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(audioAttributes,true)
            .build().apply {
                prepare(mediaSourceList)
                seekTo(currentWindow,1)
                playbackParameters = playbackParams
                setWakeMode(C.WAKE_MODE_NETWORK)
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
                        setTitle(getMetaDatafromSong(urlMediaList[windowIndex]))
                    }.build()
                }
                return MediaDescriptionCompat.Builder().build()
            }
        }
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator)
        mediaSessionConnector.setPlayer(exoPlayer)
        playerNotificationManager = PlayerNotificationManager.Builder(this,
           notificationID,
            channelID)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setSmallIconResourceId(R.drawable.ic_noti_logo)
            .build()
        playerNotificationManager.apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setUseNextAction(true)
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

    private fun getMetaDatafromSong(url: String):String{
        mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(url)
        return try {
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        } catch (e:Exception){
            Log.i("metaData: ",e.toString())
            "No title"
        }
    }

    private fun preparePlaylist():ConcatenatingMediaSource{
        mediaSourceList = ArrayList()
        mediaTitleList = ArrayList()
        for(media in urlMediaList){
            mediaSourceList.add(ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(MediaItem.fromUri(media)))
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
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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