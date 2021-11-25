package com.annhienktuit.exoplayervideoplayerzalo.utils

import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory
import com.google.android.exoplayer2.upstream.DataSpec

class CacheKeyProvider: CacheKeyFactory {
    override fun buildCacheKey(dataSpec: DataSpec): String? {
        return if (dataSpec.key != null) dataSpec.key else generateKey(dataSpec.uri.toString())
    }

    private fun generateKey(url: String): String? {
        var start = false
        var result = ""
        var stack = ArrayDeque<Char>()
        var lastCharacter = url[url.lastIndex]
        var lastIndex = url.lastIndex
        while (lastCharacter != '/'){
            if(lastCharacter == '_') start = true
            if(start){
                stack.addFirst(lastCharacter)
            }
            lastIndex --
            lastCharacter = url[lastIndex]
        }
        for(i in stack){
            result += i
        }
        result = result.dropLast(1)
        println(result)
        return result
    }
}