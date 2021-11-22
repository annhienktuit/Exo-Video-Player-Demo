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
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.checkPermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.getRealPathFromURI
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.isLandscapeOrientation
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.requestFilePermissions
import com.annhienktuit.exoplayervideoplayerzalo.utils.Extensions.toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.cache.*
import android.os.AsyncTask
import com.annhienktuit.exoplayervideoplayerzalo.utils.PreLoadingCache
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import java.lang.Exception


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
    private var mediaSourceList = ArrayList<MediaSource>()
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
            //switchLocalFile(uriLocalMedia)
        }
    }

    private fun initializePlayer() {
        val mediaList = initializeMedia()
        initializeLoadControl()
        trackSelector.parameters = trackParams
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl)
        exoPlayer.prepare(mediaList)
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

    private fun initializeMedia(): ConcatenatingMediaSource {
        mediaSourceList = ArrayList()
        var preCachingMediaURL = arrayOf(getString(R.string.pre_caching_mp3),getString(R.string.music_mp3),getString(R.string.pre_caching_mp3_2))
        mediaSource = ExtractorMediaSource(Uri.parse(getString(R.string.music_mp3)),DefaultHttpDataSourceFactory("http-useragent"),DefaultExtractorsFactory(),null,null,null)
        for(uri in preCachingMediaURL){
            val preCaching = PreLoadingCache(this)
            preCaching.execute(uri)
            mediaSourceList.add(ExtractorMediaSource(Uri.parse(uri), CacheDataSourceFactory(
                simpleCache, DefaultHttpDataSourceFactory("pre-cache")),DefaultExtractorsFactory(),null,null,null))
        }
        val concatenatingMediaSource = ConcatenatingMediaSource()
        concatenatingMediaSource.addMediaSources(mediaSourceList)
        return concatenatingMediaSource
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
            if(!isLocal){
                initializePlayer()
            }
            else {
                isLocal = false
                //switchLocalFile(uriLocalMedia)
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
               // switchLocalFile(uriLocalMedia)
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
        private var currentWindow:Int = 0
        private var playbackPosition:Long = 0L
        private var currentVolume = 0F
        private lateinit var exoPlayer:SimpleExoPlayer
        private lateinit var loadControl:LoadControl
        private lateinit var mediaSource: MediaSource
        private lateinit var mediaSourcePrecache: MediaSource
        private lateinit var dataSource: DataSource
        private var mediaSession:MediaSessionCompat? = null
    }

}
