package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.app.Application
import android.util.Log
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class CacheUtils: Application() {
    companion object{
        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize:Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
    }

    override fun onCreate() {
        super.onCreate()
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor)

    }
}