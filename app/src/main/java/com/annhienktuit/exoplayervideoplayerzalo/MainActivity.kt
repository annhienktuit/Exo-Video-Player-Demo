package com.annhienktuit.exoplayervideoplayerzalo


import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils
import com.annhienktuit.exoplayervideoplayerzalo.utils.DescriptionAdapter
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.getRealPathFromURI
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.isLandscapeOrientation
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.requestFilePermissions
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
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.source.hls.HlsMediaSource


class MainActivity : AppCompatActivity() {
    private lateinit var playerView:PlayerView
    private var trackSelector:DefaultTrackSelector = DefaultTrackSelector()
    private var trackParams:DefaultTrackSelector.Parameters = trackSelector.buildUponParameters().setMaxVideoSize(1920,1080).build()
    private val simpleCache:SimpleCache = CacheUtils.simpleCache
    private var playbackParams = PlaybackParameters(1f)
    private val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.CONTENT_TYPE_MOVIE).build()
    lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var tvResolution:TextView
    private lateinit var tvPosition:TextView
    private lateinit var btnQuality:Button
    private lateinit var btnFullScr:Button
    private lateinit var btnMute:Button
    private lateinit var btnFilePicker:Button
    private lateinit var btnSpeed:Button
    private lateinit var rlRes:RelativeLayout
    private lateinit var sharedPreferences : SharedPreferences
    private var isLocal:Boolean = false
    private var uriLocalMedia:String? = ""
    private var urlMedia:String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContentView(R.layout.activity_main)
        Log.i("lifecycle: ","onCreate")
        var extras:Bundle? = intent.extras
        if(extras !=  null){
            urlMedia = extras.getString("url").toString()
            Log.i("extras: ", urlMedia)
        }
        bindView()
        initializePlayer()
        //initializeNotification()
        restorePositionfromPrefs()
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
            startActivityForResult(intent, OPEN_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === RESULT_OK && requestCode === OPEN_REQUEST_CODE) {
            val uri: Uri? = data?.getData()
            uriLocalMedia = getRealPathFromURI(uri)
            switchLocalFile(uriLocalMedia)
        }
    }

//    private fun initializeNotification() {
//        //Notification
//        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
//        val mediaController = MediaControllerCompat(this,mediaSession.sessionToken)
//        mediaSession.isActive = true
//        mediaSessionConnector = MediaSessionConnector(mediaSession)
//        mediaSessionConnector.setPlayer(exoPlayer)
//        playerNotificationManager = PlayerNotificationManager.Builder(this, notificationID, channelID)
//            .setMediaDescriptionAdapter(DescriptionAdapter())
//            .setSmallIconResourceId(R.drawable.logo)
//            .build()
//        playerNotificationManager.apply {
//            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            setMediaSessionToken(mediaSession.sessionToken)
//            setUseNextAction(true)
//            setPlayer(exoPlayer)
//        }
//    }

    private fun initializePlayer() {
        initializeMedia()
        initializeLoadControl()
        trackSelector.parameters = trackParams
            exoPlayer = SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                .setAudioAttributes(audioAttributes,true)
                .build().apply {
                    addMediaSource(mediaSourceHighRes)
                    addMediaSource(hlsMediasource)
                    addMediaItem(MediaItem.fromUri(getString(R.string.music_mp3)))
                    if(currentPosition != C.TIME_UNSET) seekTo(currentWindow!!, playbackPosition!!)
                    playbackParameters = playbackParams
                    setWakeMode(C.WAKE_MODE_NETWORK)
                    playWhenReady = true
                    prepare()
                    isLocal = false
                }
            playerView.player = exoPlayer
            playerView.keepScreenOn = true
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

    private fun initializeMedia() {
        mediaItemHigh = MediaItem.fromUri(getString(R.string.video_mp4_high))
        mediaItemLow = MediaItem.fromUri(getString(R.string.video_mp4_low))
        httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        mediaDataSourceFactory = DefaultDataSourceFactory(this,httpDataSourceFactory) //for local media
        cacheDataSourceFactory = CacheDataSource.Factory() //for online media
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        mediaSourceHighRes = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItemHigh)
        mediaSourceLowRes = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItemLow)
        initializeHlsMediaSource()
    }

    private fun initializeHlsMediaSource(){
         hlsMediasource = HlsMediaSource.Factory(cacheDataSourceFactory)
             .setAllowChunklessPreparation(true)
             .createMediaSource(MediaItem.fromUri(getString(R.string.hls_sample)))
    }

    private fun saveCurrentPosition(){
        exoPlayer.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
        }
    }

    private fun saveToPrefs(){
        val edit = sharedPreferences.edit()
        edit.putInt("windowsPosition", currentWindow).apply()
        edit.putLong("playPosition", playbackPosition).apply()
        Log.i("sharedPref: ","save: $currentWindow $playbackPosition")
    }

    private fun restorePositionfromPrefs(){
        sharedPreferences = getSharedPreferences("position", MODE_PRIVATE)
        currentWindow = sharedPreferences.getInt("windowsPosition",1)
        playbackPosition = sharedPreferences.getLong("playPosition",100L)
        Log.i("sharedPref: ","get: $currentWindow $playbackPosition")
    }

    private fun switchHighRes(){
        exoPlayer.apply {
            playWhenReady = false
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            setMediaSource(mediaSourceHighRes)
            addMediaItem(MediaItem.fromUri(getString(R.string.music_mp3)))
            seekTo(currentWindow!!, playbackPosition!!)
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
            addMediaItem(MediaItem.fromUri(getString(R.string.music_mp3)))
            seekTo(currentWindow!!, playbackPosition!!)
            playWhenReady = true
        }
        tvResolution.text = "Low resolution"
    }

    private fun switchLocalFile(uriMedia:String?){
        if(uriMedia != null){
            initializePlayer()
            exoPlayer.playWhenReady = false
            val mediaItemFromFile = MediaItem.fromUri(uriMedia!!)
            val newMediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(mediaItemFromFile)
            exoPlayer.setMediaSource(newMediaSource)
            exoPlayer.playWhenReady = true
            isLocal = true
        }
    }

    private fun releasePlayer() {
        saveCurrentPosition()
        exoPlayer.release()
    }

    private fun hideSystemUi() {
        //Handle fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    //Handle screen rotation
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            tvResolution.visibility = View.GONE
            rlRes.visibility = View.GONE
        }
        else {
            tvResolution.visibility = View.VISIBLE
            rlRes.visibility = View.VISIBLE
        }
    }

    private fun rotateScreen(){
        if(isLandscapeOrientation()){
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            tvResolution.visibility = View.VISIBLE
            rlRes.visibility = View.VISIBLE
            btnFullScr.setBackgroundResource(R.drawable.ic_fullscreen_expand)
        }
        else {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            tvResolution.visibility = View.GONE
            rlRes.visibility = View.GONE
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


    //Handle lifecycle
    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            Log.i("lifecycle: ","onStart")
            if (mediaSession != null) {
                mediaSession.setActive(true);
            }
            if(!isLocal){
                initializePlayer()
            }
            else {
                isLocal = false
                switchLocalFile(uriLocalMedia)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Util.SDK_INT < 24)) {
            Log.i("lifecycle: ","onResume")
            if(!isLocal){
                initializePlayer()
            }
            else {
                isLocal = false
                switchLocalFile(uriLocalMedia)
            }
        }
    }
    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            Log.i("lifecycle: ","onPause")
            saveCurrentPosition()
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            Log.i("lifecycle: ","onStop")
            saveCurrentPosition()
            releasePlayer()
        }
    }

    public override fun onDestroy() {
        saveCurrentPosition()
        saveToPrefs()
        playerNotificationManager.setPlayer(null)
        releasePlayer()
        Log.i("lifecycle: ","onDestroy")
        super.onDestroy()
    }

    private fun bindView() {
        playerView = findViewById(R.id.player_view)
        tvResolution = findViewById(R.id.tvRes)
        tvPosition = findViewById(R.id.exo_position)
        btnQuality = findViewById(R.id.exo_quality_icon)
        btnFullScr = findViewById(R.id.exo_fullscreen_icon)
        btnMute = findViewById(R.id.exo_mute)
        btnFilePicker = findViewById(R.id.exo_file_picker)
        rlRes = findViewById(R.id.rlRes)
        btnSpeed = findViewById(R.id.exo_playback_speed)
    }

    companion object {
        const val OPEN_REQUEST_CODE = 1
        const val MEDIA_SESSION_TAG = "media_session"
        const val notificationID = 123
        const val channelID = "com.annhienktuit.exoplayervideoplayerzalo"
        private var currentWindow:Int = 0
        private var playbackPosition:Long = 0L
        private var currentVolume = 0F
        private lateinit var exoPlayer:SimpleExoPlayer
        private lateinit var loadControl:LoadControl
        private lateinit var mediaDataSourceFactory: DataSource.Factory
        lateinit var cacheDataSourceFactory: DataSource.Factory
        private lateinit var httpDataSourceFactory: HttpDataSource.Factory
        private lateinit var mediaSourceHighRes: ProgressiveMediaSource
        private lateinit var mediaSourceLowRes: ProgressiveMediaSource
        private lateinit var hlsMediasource: HlsMediaSource
        private lateinit var mediaItemHigh:MediaItem
        private lateinit var mediaItemLow:MediaItem
        private lateinit var mediaSession:MediaSessionCompat
        private lateinit var mediaSessionConnector: MediaSessionConnector
    }
}
