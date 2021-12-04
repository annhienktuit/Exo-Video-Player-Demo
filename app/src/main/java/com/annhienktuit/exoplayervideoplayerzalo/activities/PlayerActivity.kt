package com.annhienktuit.exoplayervideoplayerzalo.activities

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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.adapters.DescriptionAdapter
import com.annhienktuit.exoplayervideoplayerzalo.animations.RotateAnimation
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheParams
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.md5
import com.annhienktuit.exoplayervideoplayerzalo.utils.PreLoadingCache
import com.annhienktuit.exoplayervideoplayerzalo.views.CircularImageView
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import android.view.animation.LinearInterpolator

import android.view.animation.Animation

import android.animation.ObjectAnimator
import android.content.ContentResolver
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import java.util.*
import kotlin.collections.ArrayList

class PlayerActivity : AppCompatActivity() {
    private lateinit var exoPlayer:SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var tvPosition: TextView
    private lateinit var tvSongName: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnMute: Button
    private lateinit var btnSpeed: Button
    private lateinit var imgArtwork:CircularImageView
    private lateinit var rotateAnimation: RotateAnimation
    private var urlMedia:String = ""
    private lateinit var mediaTitleList: ArrayList<String>
    private lateinit var mediaURLList: ArrayList<String>
    private lateinit var mediaSourceList:ArrayList<MediaSource>
    private lateinit var mediaArtistList: ArrayList<String>
    private lateinit var mediaIDList: ArrayList<String>
    var currentVolume = 0F
    var currentWindow = 0
    private var playbackParams = PlaybackParameters(1f)
    private val audioAttribute = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MUSIC).build()
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
    private lateinit var anim: ObjectAnimator
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
        tvSongName.text = mediaTitleList[currentWindow]
        tvArtist.text = mediaArtistList[currentWindow]
        btnSpeed.setOnClickListener { changeSpeed() }
        btnMute.setOnClickListener {
            currentVolume = exoPlayer.volume
            if (currentVolume == 0f) {
                exoPlayer.volume = 1f
                btnMute.setBackgroundResource(R.drawable.ic_unmute)
            }
            else {
                exoPlayer.volume = 0f
                btnMute.setBackgroundResource(R.drawable.ic_mute)
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
            setAudioAttributes(audioAttribute, false)
            seekTo(currentWindow, 1)
            playbackParameters = playbackParams
        }
        exoPlayer.playWhenReady = true
        playerView.player = exoPlayer
        playerView.keepScreenOn = true
        initializeAnimation()
        addListener()
    }

    private fun initializeAnimation(){
        anim= ObjectAnimator.ofFloat<View>(imgArtwork, View.ROTATION, 0f, 360f).setDuration(20000)
        anim.repeatCount = Animation.INFINITE
        anim.interpolator = LinearInterpolator()
        anim.start()
    }

    private fun addListener() {
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean,playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    anim.resume()
                } else if (playWhenReady) {
                    anim.pause()
                } else {
                    anim.pause()
                }
            }

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                tvSongName.text = mediaTitleList[exoPlayer.currentWindowIndex]
                tvArtist.text = mediaArtistList[exoPlayer.currentWindowIndex]
            }
            //TODO: extract to get from metadata
//            override fun onTracksChanged(
//                trackGroups: TrackGroupArray?,
//                trackSelections: TrackSelectionArray?
//            ) {
//                super.onTracksChanged(trackGroups, trackSelections)
//                for(idx in 0 until trackGroups?.length!!){
//                    val trackGroup = trackGroups.get(idx)
//                    for(j in 0 until trackGroup.length){
//                        val trackMetaData = trackGroup.getFormat(j).metadata
//                        if(trackMetaData != null) Log.i("metaData: ",trackMetaData.get(0).toString())
//                        tvSongName.text = trackMetaData?.get(0).toString()
//                    }
//                }
//            }

        })



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
            }
        }
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator)
        mediaSessionConnector.setPlayer(exoPlayer, null)
        playerNotificationManager = PlayerNotificationManager(this, channelID, notificationID,  DescriptionAdapter(this,mediaController))
        playerNotificationManager.apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setPlayer(exoPlayer)
            setSmallIcon(R.drawable.ic_noti_logo)
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
                    DefaultExtractorsFactory(),null,null,md5(mediaIDList[i])))
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
        btnMute = findViewById(R.id.exo_mute)
        btnSpeed = findViewById(R.id.exo_playback_speed)
        imgArtwork = findViewById(R.id.exo_artwork)
        rotateAnimation = RotateAnimation(imgArtwork)
        tvArtist = findViewById(R.id.tvSongArtist)
        tvSongName = findViewById(R.id.tvSongTitle)
    }
    companion object {
        const val MEDIA_SESSION_TAG = "media_session"
        const val notificationID = 123
        const val channelID = "com.annhienktuit.exoplayervideoplayerzalo"
    }

}