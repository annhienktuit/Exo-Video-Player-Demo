package com.annhienktuit.exoplayervideoplayerzalo.utils

import android.app.PendingIntent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter

class DescriptionAdapter() : MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): String {
        val window = player.currentWindowIndex
        return window.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? = null

    override fun getCurrentContentText(player: Player): CharSequence? {
        return "Context Text Here"
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        return null
    }

}