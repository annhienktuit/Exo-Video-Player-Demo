package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils.Companion.simpleCache
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.upstream.cache.CacheUtil.getKey
import java.io.IOException


class PreLoadingCache(): AsyncTask<String, Void, Void>() {
    var uri: String? = null
    private lateinit var mContext: Context
    constructor(context:Context) : this() {
        mContext = context
    }
    override fun doInBackground(vararg uri: String): Void? {
        try {
            val thread = Thread.currentThread()
            Log.i("preloading: ", "start at thread ${thread.id}")
            this.uri = uri[0]
            val mediaUri = Uri.parse(this.uri)
            val buffer = ByteArray(CacheUtil.DEFAULT_BUFFER_SIZE_BYTES)
            val progressListener = CacheUtil.CachingCounters()
            val dataSpec = DataSpec(mediaUri)
            val upstreamDataSource = DefaultHttpDataSourceFactory("pre-cache").createDataSource()
            val dataSink = CacheDataSink(simpleCache,DEFAULT_MAX_CACHE_FILE_SIZE)
            val cacheDataSource = CacheDataSource(simpleCache, upstreamDataSource, FileDataSource(), dataSink,0, null,CacheKeyProvider())
            cacheVideo(dataSpec, cacheDataSource,buffer, progressListener)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun cacheVideo(
        dataSpec: DataSpec,
        cacheDataSource: CacheDataSource,
        buffer: ByteArray,
        progressListener: CacheUtil.CachingCounters
    ) {
        CacheUtil.cache(
            dataSpec,
            simpleCache,
            cacheDataSource,
            buffer,
            null,
            0,
            progressListener,
            null,
            false
        )
        Log.i("cachedbytes: ",progressListener.totalCachedBytes().toString())
    }
}