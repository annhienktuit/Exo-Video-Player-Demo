package com.annhienktuit.exoplayervideoplayerzalo.services

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.activities.PlayerActivity
import com.annhienktuit.exoplayervideoplayerzalo.adapters.DescriptionAdapter
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheParams
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils.Companion.simpleCache
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions
import com.annhienktuit.exoplayervideoplayerzalo.utils.PreLoadingCache
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import android.os.SystemClock

import android.widget.Toast


class MusicService : Service() {

    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var mediaTitleList: ArrayList<String>
    private lateinit var mediaURLList: ArrayList<String>
    private lateinit var mediaSourceList:ArrayList<MediaSource>
    private lateinit var mediaArtistList: ArrayList<String>
    private lateinit var mediaIDList: ArrayList<String>
    private var urlMedia:String = ""
    var currentWindow = 0
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private val audioAttribute = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MUSIC).build()
    override fun onBind(intent: Intent): IBinder {
        exoPlayer.playWhenReady = true
        getDatafromBundle(intent)
        initializePlayer()
        initializeNotification()
        return MusicServiceBinder()
    }

    private fun initializePlayer() {
        initializeDataSource()
        val mediaList = preparePlaylist()
        exoPlayer.prepare(mediaList)
        exoPlayer.seekTo(currentWindow, 1)
        exoPlayer.setAudioAttributes(audioAttribute,false)
    }


    private fun getDatafromBundle(intent: Intent) {
        val extras: Bundle? = intent.extras
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
    }

    override fun onCreate() {
        super.onCreate()
        val trackSelection = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
        val trackSelector = DefaultTrackSelector(trackSelection)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
    }

    inner class MusicServiceBinder: Binder(){
        fun getExoPlayerInstance() = exoPlayer
    }

    private fun initializeDataSource() {
        httpDataSourceFactory = DefaultHttpDataSourceFactory("pre-cache")
        mediaDataSourceFactory = DefaultDataSourceFactory(this, httpDataSourceFactory
        ) //for local media
        cacheDataSourceFactory = CacheDataSourceFactory(simpleCache, httpDataSourceFactory)
    }

    private fun preparePlaylist(): ConcatenatingMediaSource {
        mediaSourceList = ArrayList()
        for(i in 0 until mediaURLList.size){
            val preCaching = PreLoadingCache(this)
            var params = CacheParams(mediaIDList[i],mediaURLList[i])
            preCaching.execute(params)
            mediaSourceList.add(
                ExtractorMediaSource(
                    Uri.parse(mediaURLList[i]), cacheDataSourceFactory,
                    DefaultExtractorsFactory(),null,null, Extensions.md5(mediaIDList[i])
                )
            )
        }
        val concatenatingMediaSource = ConcatenatingMediaSource()
        concatenatingMediaSource.addMediaSources(mediaSourceList)
        return concatenatingMediaSource
    }

    @SuppressLint("ResourceType")
    private fun initializeNotification(){
        var mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        var mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        mediaSession.isActive = true
        var mediaSessionConnector = MediaSessionConnector(mediaSession)
        val timelineQueueNavigator = object : TimelineQueueNavigator(mediaSession) {
            @SuppressLint("ResourceType")
            override fun getMediaDescription(
                player: Player,
                windowIndex: Int
            ): MediaDescriptionCompat {
                player.let { safePlayer ->
                    return MediaDescriptionCompat.Builder().apply {
                        setTitle(mediaTitleList[exoPlayer.currentWindowIndex])
                    }.build()
                }
            }
        }
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator)
        mediaSessionConnector.setPlayer(exoPlayer, null)
        val playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, channelID, R.string.app_name, notificationID,DescriptionAdapter(this,mediaController)  )
        playerNotificationManager.apply {
            setMediaSessionToken(mediaSession.sessionToken)
            setPlayer(exoPlayer)
            setSmallIcon(R.drawable.ic_noti_logo)
        }
        playerNotificationManager.setNotificationListener(object:PlayerNotificationManager.NotificationListener{
            override fun onNotificationStarted(notificationId: Int, notification: Notification?) {
                startForeground(notificationId, notification)
            }

            override fun onNotificationCancelled(notificationId: Int) {
            }

        })
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000] =
            restartServicePendingIntent
        Toast.makeText(this,"onTaskRemoved",Toast.LENGTH_LONG).show()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show()
    }


    companion object {
        const val MEDIA_SESSION_TAG = "media_session"
        const val notificationID = 123
        const val channelID = "com.annhienktuit.exoplayervideoplayerzalo"
    }
}