package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.provider.Settings.Global.getString
import android.util.Log
import com.annhienktuit.exoplayervideoplayerzalo.R
import com.annhienktuit.exoplayervideoplayerzalo.utils.CacheUtils.Companion.simpleCache
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheUtil
import com.google.android.exoplayer2.util.Util
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
            val progressListener = CacheUtil.CachingCounters()
            val dataSpec = DataSpec(mediaUri)
            val dataSource = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "cache-preload")).createDataSource()
            cacheVideo(dataSpec,dataSource,progressListener)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun cacheVideo(
        dataSpec: DataSpec,
        dataSource: DataSource,
        progressListener: CacheUtil.CachingCounters
    ) {
        CacheUtil.cache(
            dataSpec,
            simpleCache,
            dataSource,
            progressListener,
            null
        )
        Log.i("cachedbytes: ",progressListener.totalCachedBytes().toString())
    }
}